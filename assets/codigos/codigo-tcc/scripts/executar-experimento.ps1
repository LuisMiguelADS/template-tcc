# Roda o experimento completo: para cada repetição e quantidade de usuários sobe um mock-db e uma API novos, aplica a carga com o k6, coleta CPU e memória e derruba tudo no final da rodada

# Parâmetros do experimento, qualquer um pode ser trocado na chamada, tipo -Users 100 -Repetitions 1
param(
    [ValidateSet('mvc', 'webflux', 'vt')]
    [string[]] $Apis = @('mvc', 'webflux', 'vt'),
    [int[]] $Users = @(100, 500, 1000, 2000, 5000, 10000),
    [int] $Queries = 3,
    [int] $Repetitions = 5,
    [int] $RampSec = 20,
    [int] $WarmupSec = 30,
    [int] $MeasurementSec = 120,
    [int] $CooldownSec = 30,
    [ValidateSet('closed', 'open')]
    [string] $LoadModel = 'closed',
    [int] $RateRps = 1000,
    [int] $ThinkMs = 0,
    [string[]] $Overrides = @(),
    [switch] $FixedOrder
)

# Para o script no primeiro erro e roda tudo a partir da pasta do projeto, onde fica o env e o yml do docker-compose
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

# Portas publicadas no host pelo docker-compose.yml
$ports = @{ mvc = 8081; webflux = 8082; vt = 8083 }
$mockPort = 18090

# Troca valores do .env pela linha de comando tipo -Overrides 'API_CPUS=4', já que variável de ambiente tem prioridade sobre o .env
foreach ($override in $Overrides) {
    $name, $value = $override -split '=', 2
    Set-Item -Path "env:$name" -Value $value
}

# Roda um comando do docker compose e para o script se ele falhar
function Invoke-Compose {
    docker compose @args
    if ($LASTEXITCODE -ne 0) {
        throw "Falha no comando: docker compose $args"
    }
}

# Fica chamando a URL até ela responder, pois a API demora alguns segundos para subir e devolve o JSON da resposta
function Wait-Json($url) {
    for ($i = 0; $i -lt 120; $i++) {
        try {
            return Invoke-RestMethod -Uri $url -TimeoutSec 3
        }
        catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "A URL $url nao respondeu a tempo"
}

# Lê do Docker os limites aplicados de verdade no container, para comprovar que as três APIs rodaram com os mesmos recursos
function Get-Limits($container) {
    $config = docker inspect $container --format '{{json .HostConfig}}' | ConvertFrom-Json
    return [ordered]@{
        cpus            = $config.NanoCpus / 1e9
        memoryBytes     = $config.Memory
        memorySwapBytes = $config.MemorySwap
        sysctls         = $config.Sysctls
        ulimits         = $config.Ulimits
    }
}

# Confere se o Docker Desktop está aberto antes de começar
docker info | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker nao esta em execucao. Abra o Docker Desktop e tente novamente.'
}

# Cria a pasta desta execução com a data e hora no nome tipo resultados\20261116-140000
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$outputDir = Join-Path $root "resultados\$stamp"
New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

# Remove containers que sobraram de uma execução anterior e constrói as imagens 
Write-Host 'Construindo as imagens (na primeira vez leva alguns minutos)...' -ForegroundColor Cyan
Invoke-Compose --profile load down --remove-orphans
Invoke-Compose build

# Registra o ambiente da execução como máquina, Docker, parâmetros e configuração final do compose
$computer = Get-CimInstance Win32_ComputerSystem
$wslConfigPath = Join-Path $env:USERPROFILE '.wslconfig'
if (Test-Path $wslConfigPath) {
    $wslConfig = Get-Content $wslConfigPath -Raw
}
else {
    $wslConfig = 'ausente (padrao do WSL2)'
}

$environment = [ordered]@{
    start           = (Get-Date).ToString('o')
    parameters      = [ordered]@{
        apis           = $Apis
        users          = $Users
        queries        = $Queries
        repetitions    = $Repetitions
        rampSec        = $RampSec
        warmupSec      = $WarmupSec
        measurementSec = $MeasurementSec
        cooldownSec    = $CooldownSec
        loadModel      = $LoadModel
        rateRps        = $RateRps
        thinkMs        = $ThinkMs
        fixedOrder     = [bool] $FixedOrder
        overrides      = $Overrides
    }
    host            = [ordered]@{
        os           = (Get-CimInstance Win32_OperatingSystem).Caption
        cpu          = (Get-CimInstance Win32_Processor | Select-Object -First 1).Name
        logicalCores = $computer.NumberOfLogicalProcessors
        ramGb        = [math]::Round($computer.TotalPhysicalMemory / 1GB, 1)
        powerPlan    = powercfg /getactivescheme
    }
    docker          = [ordered]@{
        server    = docker version --format '{{.Server.Version}}'
        compose   = docker compose version --short
        wslconfig = $wslConfig
    }
    images          = [ordered]@{
        mockDb     = docker image inspect tcc/mock-db:local --format '{{.Id}}'
        apiServlet = docker image inspect tcc/api-servlet:local --format '{{.Id}}'
        apiWebflux = docker image inspect tcc/api-webflux:local --format '{{.Id}}'
    }
    envFile         = Get-Content (Join-Path $root '.env') -Raw
    # Configuração do compose com todos os valores já resolvidos, registro exato do que rodou
    resolvedCompose = (docker compose --profile load config) -join "`n"
}
$environment | ConvertTo-Json -Depth 6 | Set-Content -Path (Join-Path $outputDir 'ambiente.json') -Encoding UTF8

# Executa uma rodada: sobe mock-db e API, aplica a carga, salva as medições e derruba os containers
function Invoke-Round($api, $userCount, $repetition) {
    $runId = "${api}_u${userCount}_r${repetition}"
    $statsFile = Join-Path $outputDir "$runId.stats.csv"
    $collector = $null

    try {
        # Sobe containers novos e espera responderem
        Invoke-Compose up -d --no-build --force-recreate "api-$api"
        $health = Wait-Json "http://localhost:$($ports[$api])/health"
        Wait-Json "http://localhost:$mockPort/mock/stats" | Out-Null
        $limits = [ordered]@{
            api  = Get-Limits "tcc-api-$api"
            mock = Get-Limits 'tcc-mock-db'
        }

        # Coleta CPU e memória dos dois containers em segundo plano durante toda a carga, uma linha por amostra
        'timestamp_utc;container;cpu;memory' | Set-Content -Path $statsFile -Encoding UTF8
        $collector = Start-Job -ArgumentList $statsFile, "tcc-api-$api" -ScriptBlock {
            param($file, $apiContainer)
            while ($true) {
                $timestamp = (Get-Date).ToUniversalTime().ToString('o')
                $lines = docker stats --no-stream --format '{{.Name}};{{.CPUPerc}};{{.MemUsage}}' tcc-mock-db $apiContainer
                foreach ($line in $lines) {
                    Add-Content -Path $file -Value "$timestamp;$line"
                }
            }
        }

        # Passa os parâmetros da rodada para o k6 por variáveis de ambiente, o compose usa estas no lugar das do .env
        $env:API = $api
        $env:USERS = $userCount
        $env:QUERIES = $Queries
        $env:RAMP_SEC = $RampSec
        $env:WARMUP_SEC = $WarmupSec
        $env:MEASUREMENT_SEC = $MeasurementSec
        $env:LOAD_MODEL = $LoadModel
        $env:RATE_RPS = $RateRps
        $env:THINK_MS = $ThinkMs
        $env:RUN_ID = $runId
        $env:OUT_DIR = "/resultados/$stamp"

        # Roda o k6 que grava o resumo da carga
        $startUtc = (Get-Date).ToUniversalTime()
        Invoke-Compose run --rm --no-deps --name tcc-k6 k6
        $endUtc = (Get-Date).ToUniversalTime()

        # Salva os metadados da rodada: /health da API, estatísticas do mock e limites dos containers
        $mock = Invoke-RestMethod -Uri "http://localhost:$mockPort/mock/stats" -TimeoutSec 5
        $meta = [ordered]@{
            runId          = $runId
            api            = $api
            users          = $userCount
            repetition     = $repetition
            queries        = $Queries
            measurementSec = $MeasurementSec
            startUtc       = $startUtc.ToString('o')
            endUtc         = $endUtc.ToString('o')
            health         = $health
            mock           = $mock
            limits         = $limits
        }
        $meta | ConvertTo-Json -Depth 6 | Set-Content -Path (Join-Path $outputDir "$runId.meta.json") -Encoding UTF8
    }
    finally {
        # Sempre para a coleta e derruba os containers, mesmo se a rodada falhar, para a próxima começar do zero
        if ($collector) {
            Stop-Job $collector
            Remove-Job $collector -Force
        }
        try {
            Invoke-Compose --profile load down --remove-orphans
        }
        catch {
            Write-Warning "Falha ao derrubar os containers: $($_.Exception.Message)"
        }
    }
}

# Laço principal: repetições x quantidades de usuários x APIs, com a ordem das APIs sorteada
$total = $Repetitions * $Users.Count * $Apis.Count
$round = 0
Write-Host "Experimento: $total rodadas. Resultados em: $outputDir" -ForegroundColor Cyan
Write-Host 'Deixe o notebook na tomada, com plano de energia de alto desempenho, e evite usar a maquina durante a execucao.' -ForegroundColor Yellow

for ($i = 1; $i -le $Repetitions; $i++) {
    foreach ($userCount in $Users) {
        if ($FixedOrder) {
            $order = $Apis
        }
        else {
            $order = $Apis | Get-Random -Count $Apis.Count
        }

        foreach ($api in $order) {
            $round++
            Write-Host "[$round/$total] $api | $userCount usuarios | repeticao $i" -ForegroundColor Green
            try {
                Invoke-Round $api $userCount $i
            }
            catch {
                # Uma rodada com erro não para o experimento, o erro fica anotado no falhas.log
                $message = "${api}_u${userCount}_r${i} FALHOU: $($_.Exception.Message)"
                Write-Host $message -ForegroundColor Red
                Add-Content -Path (Join-Path $outputDir 'falhas.log') -Value $message
            }

            # Pausa entre rodadas para a máquina esfriar e a próxima medição não ser afetada pela anterior
            Start-Sleep -Seconds $CooldownSec
        }
    }
}

Write-Host "Concluido. Para consolidar: .\scripts\consolidar-resultados.ps1 -Folder `"$outputDir`"" -ForegroundColor Cyan

# Junta os resultados de uma execução num CSV com uma linha por rodada e mostra as médias por API e quantidade de usuários

# Pasta da execução gerada pelo executar-experimento.ps1 
param(
    [Parameter(Mandatory)]
    [string] $Folder
)

$ErrorActionPreference = 'Stop'
$culture = [Globalization.CultureInfo]::InvariantCulture

# Converte a memória do docker stats, tipo 512.3MiB, 1.2GiB para MiB
function ConvertTo-Mib($text) {
    $number = [double]::Parse(($text -replace '[^\d.]', ''), $culture)
    if ($text -like '*GiB') { return $number * 1024 }
    if ($text -like '*MiB') { return $number }
    if ($text -like '*KiB') { return $number / 1024 }
    return $number / 1MB
}

# Calcula a média e o máximo de uma lista de amostras, com 2 casas decimais 
function Get-Stats($samples) {
    if ($samples.Count -eq 0) {
        return @{ Avg = $null; Max = $null }
    }
    $measure = $samples | Measure-Object -Average -Maximum
    return @{
        Avg = [math]::Round($measure.Average, 2)
        Max = [math]::Round($measure.Maximum, 2)
    }
}

# Monta uma linha por rodada a partir dos três arquivos que ela gerou: .meta.json, .resumo.json e .stats.csv
$rows = @()
foreach ($metaFile in Get-ChildItem -Path $Folder -Filter '*.meta.json') {
    # Metadados da rodada, gravados pelo executar-experimento.ps1
    $meta = Get-Content $metaFile.FullName -Raw | ConvertFrom-Json
    $base = Join-Path $Folder $meta.runId

    # Resumo da carga gravado pelo k6; sem ele a rodada não terminou e fica de fora
    if (-not (Test-Path "$base.resumo.json")) {
        Write-Warning "Sem resumo do k6 para $($meta.runId), rodada ignorada."
        continue
    }
    $summary = Get-Content "$base.resumo.json" -Raw | ConvertFrom-Json
    $latency = $summary.measurement.latencyMs

    # Janela de medição: os últimos segundos antes do fim do k6, o que vem antes é rampa e aquecimento
    $end = [DateTime]::Parse($summary.endUtc, $culture, 'RoundtripKind')
    $start = $end.AddSeconds(-$meta.measurementSec)

    # Separa as amostras de CPU e memória que caem dentro da janela, da API e do mock
    $apiCpu = @()
    $apiMemory = @()
    $mockCpu = @()
    if (Test-Path "$base.stats.csv") {
        $lines = Get-Content "$base.stats.csv" | Select-Object -Skip 1
        foreach ($line in $lines) {
            $timestamp, $container, $cpu, $memory = $line -split ';'
            if (-not $memory) { continue }

            $time = [DateTime]::Parse($timestamp, $culture, 'RoundtripKind')
            if ($time -lt $start -or $time -gt $end) { continue }

            $cpuValue = [double]::Parse($cpu.TrimEnd('%'), $culture)
            if ($container -eq 'tcc-mock-db') {
                $mockCpu += $cpuValue
            }
            else {
                $usedMemory = ($memory -split '/')[0].Trim()
                $apiCpu += $cpuValue
                $apiMemory += ConvertTo-Mib $usedMemory
            }
        }
    }
    $apiCpuStats = Get-Stats $apiCpu
    $apiMemoryStats = Get-Stats $apiMemory
    $mockCpuStats = Get-Stats $mockCpu

    $rows += [pscustomobject]@{
        api                          = $meta.api
        users                        = $meta.users
        repetition                   = $meta.repetition
        requests                     = $summary.measurement.requests
        throughput_rps               = [math]::Round($summary.measurement.throughputRps, 2)
        error_rate_pct               = [math]::Round($summary.measurement.errorRate * 100, 4)
        lat_avg_ms                   = [math]::Round($latency.avg, 2)
        lat_median_ms                = [math]::Round($latency.med, 2)
        lat_p90_ms                   = [math]::Round($latency.'p(90)', 2)
        lat_p95_ms                   = [math]::Round($latency.'p(95)', 2)
        lat_p99_ms                   = [math]::Round($latency.'p(99)', 2)
        lat_max_ms                   = [math]::Round($latency.max, 2)
        api_cpu_avg_pct              = $apiCpuStats.Avg
        api_cpu_max_pct              = $apiCpuStats.Max
        api_mem_avg_mib              = $apiMemoryStats.Avg
        api_mem_max_mib              = $apiMemoryStats.Max
        mock_cpu_avg_pct             = $mockCpuStats.Avg
        mock_peak_concurrent_queries = $meta.mock.peakInProgress
        java                         = $meta.health.java
        virtual_thread               = $meta.health.virtualRequestThread
    }
}

if ($rows.Count -eq 0) {
    throw "Nenhuma rodada completa encontrada em $Folder"
}

# Grava o CSV com uma linha por rodada, que é o arquivo usado na análise estatística
$csvPath = Join-Path $Folder 'consolidado.csv'
$rows | Sort-Object api, users, repetition | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8 -Delimiter ';'
Write-Host "Gerado: $csvPath" -ForegroundColor Cyan

# Mostra na tela a média das repetições de cada API em cada quantidade de usuários
$groups = $rows | Group-Object api, users
$overview = foreach ($group in $groups) {
    $rounds = $group.Group
    [pscustomobject]@{
        api            = $rounds[0].api
        users          = $rounds[0].users
        n              = $rounds.Count
        throughput_rps = [math]::Round(($rounds | Measure-Object throughput_rps -Average).Average, 1)
        lat_p95_ms     = [math]::Round(($rounds | Measure-Object lat_p95_ms -Average).Average, 1)
        lat_p99_ms     = [math]::Round(($rounds | Measure-Object lat_p99_ms -Average).Average, 1)
        error_pct      = [math]::Round(($rounds | Measure-Object error_rate_pct -Average).Average, 3)
        cpu_pct        = [math]::Round(($rounds | Measure-Object api_cpu_avg_pct -Average).Average, 1)
        mem_mib        = [math]::Round(($rounds | Measure-Object api_mem_avg_mib -Average).Average, 1)
    }
}
$overview | Sort-Object users, api | Format-Table -AutoSize

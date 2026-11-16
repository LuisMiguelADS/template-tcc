// capitulos/01-introducao.typ — Capítulo 1: Introdução.
// Recursos do template demonstrados aqui: #abbrev (siglas), #gloss (glossário),
// #cite e #prose (citações indiretas), nota de rodapé, #figura com fonte de terceiros,
// referência cruzada com @rótulo e lista não numerada.
// Pode ser aberto sozinho no preview: o #show: capitulo.with(...) aplica o estilo e a numeração.
// Rótulos de outros capítulos aparecem como ‹rótulo› em vermelho; no main.typ, saem certos.
#import "../ifbasaj-tcc/imports.typ": *
#show: capitulo.with(numero: 1)

= Introdução <cap-introducao>

== Contextualização <sec-contextualizacao>

Com a crescente adoção da arquitetura de microsserviços no desenvolvimento de novos sistemas, valorizado pelo seus benefícios como escalabilidade, manutenibilidade, agilidade e autonomia das equipes, procuram-se formas eficientes para atender cenários de alta concorrência impulsionado pelo próprio modelo arquitetural, sem aumentar de forma siginificativa os custos operacionais @devalla2024. Ao longo da história do Java, a abordagem bloqueante baseada no modelo clássico de threads, na qual cada requisição ocupa integralmente uma thread do sistema para cada tarefa, sempre foi predominante nas aplicações @jep444. Porém, este modelo oferece graves gargalos em cenários de alta concorrência, em busca de contornar este problema a programação reativa do Spring WebFlux ganhou força, onde a thread do pool apenas recebe a tarefa e delega a execução, ficando livre para novas solicitações @devalla2024. Diante deste embate entre as duas abordagens, surgiu no Java 21 o Project Loom, trazendo Threads Virtuais para o ecossistema Java @jep444. Por serem significativamente mais leves que as threads do sistema operacional, é possível instanciar milhares delas e designar uma para cada tarefa, prometendo equilibrar esse cenário ao unir a simplicidade da abordagem bloqueante com a eficiência da abordagem reativa @devalla2024.


== Problema de pesquisa <sec-problema>

Embora a abordagem não bloqueante do Spring WebFlux tenha surgido para contornar gargalos da abordagem bloqueante do Spring MVC, sua adoção enfrenta barreiras devido a maior complexidade de desenvolvimento, manutenção e depuração do código @devalla2024. Diante do surgimento das threads virtuais introduzida na versão 21 do Java a partir do Project Loom, espera-se alcançar a eficiência da abordagem não bloqueante mantendo a simplicidade de desenvolvimento do modelo bloqueante @jep444, mas identifica-se uma escassez de avaliações experimentais diretas comparando essas três abordagens sob o mesmo ambiente padronizado de microsserviços.  

Diante deste cenário, submetidos as mesmas condições de infraestrutura e testes de carga intensivos de I/O em patamares graduais de concorrência de 100, 500, 1.000, 2.000, 5.000 e 10.000 usuário simultâneos, como as abordagens baseadas em Spring MVC, Spring WebFlux e Threads Virtuais se comparam em termos de throughout, latência p95 e p99, taxa de erros e consumo de memória RAM e CPU?

Ficam de fora do escopo deste trabalho:
- Mensurar ou analisar de forma direta os custos financeiros em provedores de infraestrutura em nuvem.
- Comparação com frameworks ou runtimes de outros ecossistemas.
- Avaliação de experiência do usuário ou teste de usabilidade.
- 

== Hipótese <sec-hipotese>

Sob condições intensivas de carga e operações predominantemente limitadas por I/O, projeta-se que o modelo de threads virtuais do Project Loom apresente desempenho e escalabilidade superiores à abordagem síncrona tradicional do Spring MVC, alcançando um throughput e uma latência p95-p99 próximos aos do Spring WebFlux. Em termos de consumo de recursos computacionais, espera-se que as threads virtuais reduzam o uso de CPU e o tempo gasto com troca de contexto em relação ao Spring MVC, mantendo uma taxa de erros reduzida e exigindo um overhead de memória RAM significativamente inferior ao das threads de plataforma do sistema operacional.

== Objetivo geral <sec-objetivo-geral>

Analisar os impactos das abordagens bloqueante, não bloqueante e das threads virtuais em termos de escalabilidade, desempenho e consumo de recursos computacionais, em um sistema Java sob alta concorrência.

== Objetivos específicos <sec-objetivos-especificos>

- Desenvolver microsserviços Java para as três abordagens, mantendo de maneira rígida as regras de negócio, infraestrutura de banco de dados e o ambiente de execução;
- Realizar testes de carga de forma controlada simulando cenários de alta concorrência de I/O;
- Coletar e analisar as métricas de desempenho e recursos, como throughout, tempo de resposta, latência p95 e p99, taxas de erro, overhead de memória RAM e uso de CPU;
- Comparar de forma prática os resultados para fornecer um caminho analítico que ajude na tomada de decisão arquitetural em ambientes de microsserviços suscetíveis a alta concorrência de I/O.

== Justificativa <sec-justificativa>

O desenvolvimento deste trabalho justifica-se pela complexidade na gestão de concorrência em sistemas Java baseados em microsserviços @devalla2024. Embora o ecossistema Java aborde individualmente diferentes modelos de concorrência, no levantamento bibliográfico realizado observa-se uma escassez de estudos experimentais que confrontem diretamente e de forma integrada o desempenho dessas abordagens sob idênticas condições de carga. Assim, a pesquisa mostra-se relevante ao fornecer recursos técnicos para tomadas de decisão arquitetural, auxiliando no desenvolvimento de sistemas mais resilientes e economicamente sustentáveis, capazes de otimizar o hardware existente antes de recorrer ao escalonamento horizontal ou vertical em ambientes de nuvem @aws_microservices.

== Organização do trabalho <sec-organizacao>

#orientacao[
  Escreva por último, quando todos os capítulos existirem. Um parágrafo curto por capítulo. Use referências cruzadas: se um título mudar, o número se atualiza sozinho.
]

O @cap-referencial apresenta ... O @cap-correlatos compara ... O @cap-metodologia descreve ... O @cap-desenvolvimento detalha ... O @cap-resultados discute ... Por fim, o @cap-consideracoes retoma os objetivos e aponta trabalhos futuros.

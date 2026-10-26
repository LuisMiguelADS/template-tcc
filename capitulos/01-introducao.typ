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

Com o avanço na utilização da arquitetura de microsserviços na criação de novos sistemas, valorizado pelo seus benefícios na computação em nuvem, procura-se formas eficientes para otimizar o uso de recursos computacionais e capacidade de atender cenários de alta concorrência sem aumentar de forma significativa os custos operacionais. Ao longo da história do Java a abordagem bloqueante do Spring MVC, na qual cada requisição consome uma Thread do sistema para cada tarefa do início ao fim, sempre foi predominante nas aplicações. Porém este modelo gera gargalos de memória em cenários de alta concorrência, em busca de contornar este problema a programação reativa do Spring WebFlux ganhou força, a Thread agora apenas recebe a tarefa e manda executar em segundo plano, ficando livre para receber novas tarefas. Diante deste embate entre as duas abordagens surgiu no Java 21 o Project Loom, trazendo Threads Virtuais para o ecossistema Java, elas são significativamente mais leves que as Threads da máquina, com isso é possível instanciar milhares de Threads Virtuais e designar uma para cada tarefa, onde promete equilibrar esse cenário com a simplicidade da abordagem bloqueante e a eficiência da abordagem reativa.

== Problema de pesquisa <sec-problema>

Embora seja reconhecido os benefícios da abordagem reativa presente no Java com Spring WebFlux e o surgimento das Threads Virtuais no Java 21 com o Project Loom, identifica-se uma certa escassez em dados experimentais e concretos que comparem os impactos na utilização dessas três abordagens no cenário de microsserviços sob o mesmo ambiente de teste e submetido a testes de carga.

== Hipótese <sec-hipotese>

Projeta-se que a utilização das Threads Virtuais do Project Loom entregue resultados eficientes em questões de escalabilidade e uso de recursos computacionais, reduzindo o Context Switching causado pela abordagem bloqueante do Spring MVC e chegando a resultados parecidos ou superiores ao Spring WebFlux. Dessa forma, superando de forma significativa a abordagem bloqueante e mantendo o desenvolvimento mais prático e simples anulando a complexidade da abordagem reativa.

== Objetivo geral <sec-objetivo-geral>

Analisar os impactos das abordagens bloqueante, não bloqueante e das Threads Virtuais em termos de escalabilidade, desempenho e consumo de recursos computacionais, em um sistema Java sob alta concorrência.

== Objetivos específicos <sec-objetivos-especificos>

Desenvolver microsserviços Java para as três abordagens, mantendo de maneira rígida as regras de negócio, infraestrutura de banco de dados e o ambiente de execução.
Realizar testes de carga de forma controlada simulando cenários de alta concorrência de I/O.
Coletar e analisar as métricas de desempenho e recursos, como throughout, tempo de resposta, latência p95 e p99, taxas de erro, overhead de memória RAM e uso de CPU.
Comparar de forma prática os resultados para fornecer um caminho analítico que ajude na tomada de decisão arquitetural em ambientes de microsserviços suscetíveis a alta concorrência de I/O.

== Justificativa <sec-justificativa>

O desenvolvimento deste trabalho justifica-se pela complexidade na gestão de concorrência em sistemas Java baseados em microsserviços. Embora o ecossistema Java aborde individualmente diferentes modelos de concorrência, no levantamento bibliográfico realizado observa-se uma escassez de estudos comparativos diretos quanto ao impacto dessas escolhas no consumo de recursos e no desempenho. Assim, a pesquisa mostra-se relevante ao fornecer recursos técnicos para tomadas de decisões na escolha do design arquitetural, auxiliando no desenvolvimento de sistemas mais resilientes e economicamente sustentáveis, que otimizam o hardware existente antes de recorrer ao aumento da capacidade em ambientes de nuvem.

== Organização do trabalho <sec-organizacao>

#orientacao[
  Escreva por último, quando todos os capítulos existirem. Um parágrafo curto por capítulo. Use referências cruzadas: se um título mudar, o número se atualiza sozinho.
]

O @cap-referencial apresenta ... O @cap-correlatos compara ... O @cap-metodologia descreve ... O @cap-desenvolvimento detalha ... O @cap-resultados discute ... Por fim, o @cap-consideracoes retoma os objetivos e aponta trabalhos futuros.

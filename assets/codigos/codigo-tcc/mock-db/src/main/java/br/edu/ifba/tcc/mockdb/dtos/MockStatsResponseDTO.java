package br.edu.ifba.tcc.mockdb.dtos;

// Estatísticas do banco simulado em /mock/stats, as chaves do JSON são lidas pelos scripts do experimento e não devem ser renomeadas sem atualizar eles
/**
 * @param latencyMs Latência configurada para cada consulta
 * @param jitterMs Variação configurada para mais ou para menos em torno da latência
 * @param totalQueries Consultas atendidas desde que o mock subiu
 * @param inProgress Consultas esperando a latência neste momento
 * @param peakInProgress Maior número de consultas simultâneas já visto, comprova a concorrência atingida na rodada
 */
public record MockStatsResponseDTO(
        long latencyMs,
        long jitterMs,
        long totalQueries,
        int inProgress,
        int peakInProgress
) {}

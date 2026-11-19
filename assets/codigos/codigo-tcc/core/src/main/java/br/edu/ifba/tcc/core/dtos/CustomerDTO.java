package br.edu.ifba.tcc.core.dtos;

// Linha da "tabela" de clientes do banco simulado, é a 1ª consulta da cadeia e o mesmo record é usado pelo mock-db e pelas APIs
/**
 * @param id Id do cliente
 * @param name Nome do cliente
 * @param segment Segmento do cliente (RETAIL, CORPORATE ou WHOLESALE), usado na consulta de pedidos
 */
public record CustomerDTO(
        long id,
        String name,
        String segment
) {}

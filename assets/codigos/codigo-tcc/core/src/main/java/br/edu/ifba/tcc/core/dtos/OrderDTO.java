package br.edu.ifba.tcc.core.dtos;

import java.math.BigDecimal;

// Linha da "tabela" de pedidos do banco simulado, é a 2ª consulta da cadeia e cada cliente tem sempre 10 pedidos
/**
 * @param id Id do pedido, no formato customerId * 100 + número do pedido (ex.: 701 a 710 para o cliente 7)
 * @param customerId Id do cliente dono do pedido
 * @param amount Valor do pedido em reais, a soma de todos define a classificação do cliente
 * @param status Situação do pedido (PAID, SHIPPED ou DELIVERED), não entra no cálculo
 */
public record OrderDTO(
        long id,
        long customerId,
        BigDecimal amount,
        String status
) {}

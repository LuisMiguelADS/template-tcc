package br.edu.ifba.tcc.core.dtos;

import java.math.BigDecimal;

// Linha da "tabela" de itens do banco simulado, é a 3ª consulta em diante e cada pedido tem sempre 5 itens
/**
 * @param orderId Id do pedido ao qual o item pertence
 * @param sku Código do produto
 * @param quantity Quantidade comprada
 * @param price Preço unitário em reais
 */
public record OrderItemDTO(
        long orderId,
        String sku,
        int quantity,
        BigDecimal price
) {}

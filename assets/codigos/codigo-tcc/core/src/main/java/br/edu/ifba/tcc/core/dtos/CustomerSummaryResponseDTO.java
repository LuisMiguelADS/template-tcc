package br.edu.ifba.tcc.core.dtos;

import java.math.BigDecimal;

// Resposta do endpoint de resumo, igual nas três APIs para garantir que todas fazem o mesmo trabalho e a comparação meça só o modelo de concorrência
/**
 * @param customerId Id do cliente consultado
 * @param name Nome do cliente
 * @param segment Segmento do cliente
 * @param totalOrders Quantidade de pedidos lidos, fica 0 quando queries = 1
 * @param totalAmount Soma do valor dos pedidos em reais, com 2 casas decimais
 * @param averageTicket Valor médio por pedido em reais, com 2 casas decimais
 * @param analyzedItems Quantidade de itens lidos nas consultas de itens
 * @param itemsAmount Soma de quantidade x preço dos itens lidos em reais, com 2 casas decimais
 * @param tier Classificação do cliente (GOLD, SILVER ou BRONZE)
 * @param discountPercent Desconto concedido pela classificação, com +2 se o cliente for corporativo
 */
public record CustomerSummaryResponseDTO(
        long customerId,
        String name,
        String segment,
        int totalOrders,
        BigDecimal totalAmount,
        BigDecimal averageTicket,
        int analyzedItems,
        BigDecimal itemsAmount,
        String tier,
        int discountPercent
) {}

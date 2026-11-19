package br.edu.ifba.tcc.core.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.CustomerSummaryResponseDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;

// Regra de negócio única do experimento, usada igual pelas três APIs para o trabalho de CPU por requisição ser o mesmo em todas
public final class CustomerSummaryCalculator {

    // Limites da classificação em reais; BigDecimal evita os erros de arredondamento do double com dinheiro
    static final BigDecimal GOLD_THRESHOLD = new BigDecimal("5000.00");
    static final BigDecimal SILVER_THRESHOLD = new BigDecimal("4000.00");
    static final String CORPORATE_SEGMENT = "CORPORATE";
    // Valores monetários da resposta sempre com 2 casas decimais
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE);

    private CustomerSummaryCalculator() {
    }

    /**
     * @param customer Cliente retornado pela 1ª consulta
     * @param orders Pedidos do cliente, fica vazio quando a requisição tem só 1
     * consulta
     * @param itemsByOrder Itens de cada pedido consultado, só os primeiros
     * pedidos têm os itens lidos
     * @return Resumo do cliente pronto para virar a resposta JSON, com os
     * valores em reais
     */
    public static CustomerSummaryResponseDTO calculate(CustomerDTO customer, List<OrderDTO> orders,
            List<List<OrderItemDTO>> itemsByOrder) {
        // Valor total e ticket médio dos pedidos
        BigDecimal totalAmount = ZERO;
        for (OrderDTO order : orders) {
            totalAmount = totalAmount.add(order.amount());
        }
        // Ticket médio arredondado para o centavo mais próximo
        BigDecimal averageTicket = orders.isEmpty()
                ? ZERO
                : totalAmount.divide(BigDecimal.valueOf(orders.size()), MONEY_SCALE, RoundingMode.HALF_UP);

        // Quantidade e valor dos itens que foram consultados
        int analyzedItems = 0;
        BigDecimal itemsAmount = ZERO;
        for (List<OrderItemDTO> items : itemsByOrder) {
            for (OrderItemDTO item : items) {
                analyzedItems++;
                itemsAmount = itemsAmount.add(item.price().multiply(BigDecimal.valueOf(item.quantity())));
            }
        }

        // Classificação pelo valor total: GOLD com 10%, SILVER com 5% e BRONZE sem desconto
        String tier;
        int discount;
        if (totalAmount.compareTo(GOLD_THRESHOLD) >= 0) {
            tier = "GOLD";
            discount = 10;
        } else if (totalAmount.compareTo(SILVER_THRESHOLD) >= 0) {
            tier = "SILVER";
            discount = 5;
        } else {
            tier = "BRONZE";
            discount = 0;
        }
        // Cliente corporativo ganha mais 2 pontos de desconto em qualquer classificação
        if (CORPORATE_SEGMENT.equals(customer.segment())) {
            discount += 2;
        }

        return new CustomerSummaryResponseDTO(customer.id(), customer.name(), customer.segment(), orders.size(),
                totalAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP), averageTicket, analyzedItems,
                itemsAmount.setScale(MONEY_SCALE, RoundingMode.HALF_UP), tier, discount);
    }
}

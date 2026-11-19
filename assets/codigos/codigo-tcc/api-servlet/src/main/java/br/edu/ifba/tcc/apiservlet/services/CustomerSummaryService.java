package br.edu.ifba.tcc.apiservlet.services;

import br.edu.ifba.tcc.apiservlet.integrations.mockdb.MockDbClient;
import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.CustomerSummaryResponseDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;
import br.edu.ifba.tcc.core.services.CustomerSummaryCalculator;
import br.edu.ifba.tcc.core.services.QueryIntensity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Faz a cadeia de consultas ao banco simulado uma depois da outra e entrega os dados para a regra de negócio do core
@Service
@RequiredArgsConstructor
public class CustomerSummaryService {

    private final MockDbClient mockDbClient;

    /**
     * @param customerId Id do cliente
     * @param queries Quantidade de consultas ao banco, entre 1 e 12
     * @return Resumo do cliente calculado pela regra de negócio
     */
    // Se a quantidade de consultas for diferente de 1 a 12 lança IllegalArgumentException, que vira 400
    public CustomerSummaryResponseDTO summarize(long customerId, int queries) {
        // Valida antes de qualquer ida ao banco, assim requisição inválida não gera I/O
        QueryIntensity.requireValid(queries);

        // Consulta 1: o cliente
        CustomerDTO customer = mockDbClient.findCustomer(customerId);

        List<OrderDTO> orders = List.of();
        List<List<OrderItemDTO>> itemsByOrder = new ArrayList<>();
        if (QueryIntensity.fetchesOrders(queries)) {
            // Consulta 2: os pedidos, que dependem do segmento do cliente
            orders = mockDbClient.findOrders(customerId, customer.segment());

            // Consultas 3 a N: itens dos primeiros pedidos, um de cada vez
            int ordersWithItems = QueryIntensity.ordersWithItems(queries, orders.size());
            for (int i = 0; i < ordersWithItems; i++) {
                itemsByOrder.add(mockDbClient.findItems(orders.get(i).id()));
            }
        }

        return CustomerSummaryCalculator.calculate(customer, orders, itemsByOrder);
    }
}

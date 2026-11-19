package br.edu.ifba.tcc.apiwebflux.services;

import br.edu.ifba.tcc.apiwebflux.integrations.mockdb.MockDbClient;
import br.edu.ifba.tcc.core.dtos.CustomerSummaryResponseDTO;
import br.edu.ifba.tcc.core.services.CustomerSummaryCalculator;
import br.edu.ifba.tcc.core.services.QueryIntensity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

// Faz a mesma cadeia de consultas da api-servlet, mas encadeando os passos com o Reactor em vez de esperar cada resposta
@Service
@RequiredArgsConstructor
public class CustomerSummaryService {

    private final MockDbClient mockDbClient;

    /**
     * @param customerId Id do cliente
     * @param queries Quantidade de consultas ao banco, entre 1 e 12
     * @return Mono que executa a cadeia quando assinado e entrega o resumo do cliente
     */
    // Se a quantidade de consultas for diferente de 1 a 12 lança IllegalArgumentException, que vira 400
    public Mono<CustomerSummaryResponseDTO> summarize(long customerId, int queries) {
        // Valida antes de montar a cadeia, assim requisição inválida não gera I/O
        QueryIntensity.requireValid(queries);

        // Consulta 1: o cliente; o flatMap executa o próximo passo quando o resultado chegar
        return mockDbClient.findCustomer(customerId).flatMap(customer -> {
            if (!QueryIntensity.fetchesOrders(queries)) {
                return Mono.just(CustomerSummaryCalculator.calculate(customer, List.of(), List.of()));
            }
            // Consulta 2: os pedidos, que dependem do segmento do cliente
            return mockDbClient.findOrders(customerId, customer.segment()).flatMap(orders -> {
                int ordersWithItems = QueryIntensity.ordersWithItems(queries, orders.size());
                // Consultas 3 a N: o concatMap só faz a próxima consulta de itens quando a anterior termina
                // (o flatMap faria todas em paralelo e mudaria a carga do experimento)
                return Flux.range(0, ordersWithItems)
                        .concatMap(i -> mockDbClient.findItems(orders.get(i).id()))
                        .collectList()
                        .map(itemsByOrder -> CustomerSummaryCalculator.calculate(customer, orders, itemsByOrder));
            });
        });
    }
}

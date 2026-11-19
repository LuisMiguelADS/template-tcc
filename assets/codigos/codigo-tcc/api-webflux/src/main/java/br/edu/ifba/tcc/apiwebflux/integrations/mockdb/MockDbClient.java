package br.edu.ifba.tcc.apiwebflux.integrations.mockdb;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;
import br.edu.ifba.tcc.core.exceptions.ExternalServiceException;
import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

// Cliente reativo do banco simulado, os métodos devolvem um Mono na hora e nenhuma thread fica parada esperando a resposta
@Component
public class MockDbClient {

    // Necessário para o Jackson-JSON saber que a resposta é uma lista de OrderDTO/OrderItemDTO, já que o Java apaga o tipo da lista em tempo de execução
    private static final ParameterizedTypeReference<List<OrderDTO>> ORDERS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<OrderItemDTO>> ITEMS = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;

    /**
     * @param baseUrl Endereço do mock-db, dentro do Docker é
     * http://mock-db:8080
     * @param maxConnections Tamanho máximo do pool de conexões com o mock-db
     * @param timeoutMs Tempo máximo para conectar, para conseguir conexão do
     * pool e para esperar a resposta
     */
    // Construtor usado pelo Spring, com os valores vindos do application.properties
    @Autowired
    public MockDbClient(@Value("${tcc.mock-db.base-url}") String baseUrl,
            @Value("${tcc.mock-db.max-connections}") int maxConnections,
            @Value("${tcc.mock-db.timeout-ms}") long timeoutMs) {
        this(buildWebClient(baseUrl, maxConnections, timeoutMs));
    }

    // Construtor usado pelos testes, recebe um WebClient montado à parte
    MockDbClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // Monta o WebClient sobre um pool de conexões grande, já que o padrão do Reactor Netty (2 x núcleos, no mínimo 16) viraria o gargalo
    private static WebClient buildWebClient(String baseUrl, int maxConnections, long timeoutMs) {
        ConnectionProvider connections = ConnectionProvider.builder("mock-db")
                .maxConnections(maxConnections)
                // Quantas requisições podem esperar por uma conexão livre antes de falhar
                .pendingAcquireMaxCount(maxConnections * 2)
                // Quanto tempo uma requisição espera por uma conexão livre
                .pendingAcquireTimeout(Duration.ofMillis(timeoutMs))
                .build();
        HttpClient httpClient = HttpClient.create(connections)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * @param id Id do cliente
     * @return Mono com os dados do cliente, é a 1ª consulta da cadeia
     */
    // O onErrorMap troca qualquer falha por ExternalServiceException, que o GlobalExceptionHandler transforma em 502
    public Mono<CustomerDTO> findCustomer(long id) {
        return webClient.get().uri("/db/clients/{id}", id).retrieve().bodyToMono(CustomerDTO.class)
                .onErrorMap(WebClientException.class,
                        e -> new ExternalServiceException("Falha ao consultar o cliente " + id + " no banco simulado", e));
    }

    /**
     * @param customerId Id do cliente
     * @param segment Segmento vindo da 1ª consulta, o que obriga a cadeia a ser
     * sequencial
     * @return Mono com os pedidos do cliente, é a 2ª consulta da cadeia
     */
    public Mono<List<OrderDTO>> findOrders(long customerId, String segment) {
        return webClient.get().uri("/db/clients/{id}/orders?segment={segment}", customerId, segment)
                .retrieve().bodyToMono(ORDERS)
                .onErrorMap(WebClientException.class,
                        e -> new ExternalServiceException("Falha ao consultar os pedidos do cliente " + customerId, e));
    }

    /**
     * @param orderId Id do pedido
     * @return Mono com os itens do pedido, da 3ª consulta em diante
     */
    public Mono<List<OrderItemDTO>> findItems(long orderId) {
        return webClient.get().uri("/db/orders/{id}/items", orderId).retrieve().bodyToMono(ITEMS)
                .onErrorMap(WebClientException.class,
                        e -> new ExternalServiceException("Falha ao consultar os itens do pedido " + orderId, e));
    }
}

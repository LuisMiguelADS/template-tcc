package br.edu.ifba.tcc.apiservlet.integrations.mockdb;

import java.util.List;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;
import br.edu.ifba.tcc.core.exceptions.ExternalServiceException;

// Cliente bloqueante do banco simulado, cada chamada deixa a thread parada até a resposta chegar, que é o que diferencia as variantes mvc e vt
@Component
public class MockDbClient {

    // Necessário para o Jackson-json saber que a resposta é uma lista de OrderDTO/OrderItemDTO, já que o Java apaga o tipo da lista em tempo de execução
    private static final ParameterizedTypeReference<List<OrderDTO>> ORDERS = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<OrderItemDTO>> ITEMS = new ParameterizedTypeReference<>() {
    };

    private final RestClient restClient;

    /**
     * @param baseUrl Endereço do mock-db, dentro do Docker é
     * http://mock-db:8080
     * @param maxConnections Tamanho máximo do pool de conexões com o mock-db
     * @param timeoutMs Tempo máximo para conectar e para esperar a resposta
     */
    // Construtor usado pelo Spring, com os valores vindos do application.properties
    @Autowired
    public MockDbClient(@Value("${tcc.mock-db.base-url}") String baseUrl,
            @Value("${tcc.mock-db.max-connections}") int maxConnections,
            @Value("${tcc.mock-db.timeout-ms}") long timeoutMs) {
        this(buildRestClient(baseUrl, maxConnections, timeoutMs));
    }

    // Construtor usado pelos testes, recebe um RestClient ligado a um servidor falso
    MockDbClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // Monta o RestClient sobre um pool de conexões grande, para o limite de conexões com o banco nunca ser o gargalo medido
    private static RestClient buildRestClient(String baseUrl, int maxConnections, long timeoutMs) {
        PoolingHttpClientConnectionManager connections = PoolingHttpClientConnectionManagerBuilder.create()
                // Pool com menos travas internas, melhor quando milhares de threads virtuais disputam conexão
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                // Reaproveita a conexão usada por último
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                // Só existe um destino, então o limite total e o por rota são iguais
                .setMaxConnTotal(maxConnections)
                .setMaxConnPerRoute(maxConnections)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(timeoutMs))
                        .setSocketTimeout(Timeout.ofMilliseconds(timeoutMs))
                        .build())
                .build();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connections).build();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

    /**
     * @param id Id do cliente
     * @return Dados do cliente, é a 1ª consulta da cadeia
     */
    // Qualquer falha na chamada vira ExternalServiceException, que o GlobalExceptionHandler transforma em 502
    public CustomerDTO findCustomer(long id) {
        try {
            return restClient.get().uri("/db/clients/{id}", id).retrieve().body(CustomerDTO.class);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Falha ao consultar o cliente " + id + " no banco simulado", e);
        }
    }

    /**
     * @param customerId Id do cliente
     * @param segment Segmento vindo da 1ª consulta, o que obriga a cadeia a ser
     * sequencial
     * @return Pedidos do cliente, é a 2ª consulta da cadeia
     */
    public List<OrderDTO> findOrders(long customerId, String segment) {
        try {
            return restClient.get().uri("/db/clients/{id}/orders?segment={segment}", customerId, segment)
                    .retrieve().body(ORDERS);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Falha ao consultar os pedidos do cliente " + customerId, e);
        }
    }

    /**
     * @param orderId Id do pedido
     * @return Itens do pedido, da 3ª consulta em diante (uma chamada por
     * pedido)
     */
    public List<OrderItemDTO> findItems(long orderId) {
        try {
            return restClient.get().uri("/db/orders/{id}/items", orderId).retrieve().body(ITEMS);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Falha ao consultar os itens do pedido " + orderId, e);
        }
    }
}

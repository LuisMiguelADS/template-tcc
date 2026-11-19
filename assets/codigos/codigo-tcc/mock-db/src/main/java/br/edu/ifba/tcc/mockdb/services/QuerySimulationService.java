package br.edu.ifba.tcc.mockdb.services;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;
import br.edu.ifba.tcc.mockdb.dtos.MockStatsResponseDTO;
import br.edu.ifba.tcc.mockdb.repositories.MockDataRepository;
import reactor.core.publisher.Mono;

// Simula o tempo de cada consulta com um temporizador, sem prender thread na espera, e conta o pico de consultas simultâneas para comprovar a concorrência atingida
@Service
public class QuerySimulationService {

    private final MockDataRepository mockDataRepository;
    private final long latencyMs;
    private final long jitterMs;
    // Contadores atômicos porque várias threads do Netty atualizam eles ao mesmo tempo
    private final AtomicInteger inProgress = new AtomicInteger();
    private final AtomicInteger peakInProgress = new AtomicInteger();
    private final LongAdder totalQueries = new LongAdder();

    /**
     * @param mockDataRepository Gerador das linhas devolvidas
     * @param latencyMs Tempo de cada consulta, 50 ms por padrão
     * @param jitterMs Variação aleatória para mais ou para menos em torno da
     * latência, 0 por padrão
     */
    public QuerySimulationService(MockDataRepository mockDataRepository,
            @Value("${tcc.mock-db.latency-ms}") long latencyMs,
            @Value("${tcc.mock-db.jitter-ms}") long jitterMs) {
        this.mockDataRepository = mockDataRepository;
        this.latencyMs = latencyMs;
        this.jitterMs = jitterMs;
    }

    /**
     * @param id Id do cliente
     * @return Mono com o cliente, entregue depois da latência simulada
     */
    public Mono<CustomerDTO> findCustomer(long id) {
        return simulate(() -> mockDataRepository.findCustomer(id));
    }

    /**
     * @param customerId Id do cliente
     * @return Mono com os pedidos do cliente, entregue depois da latência
     * simulada
     */
    public Mono<List<OrderDTO>> findOrders(long customerId) {
        return simulate(() -> mockDataRepository.findOrders(customerId));
    }

    /**
     * @param orderId Id do pedido
     * @return Mono com os itens do pedido, entregue depois da latência simulada
     */
    public Mono<List<OrderItemDTO>> findItems(long orderId) {
        return simulate(() -> mockDataRepository.findItems(orderId));
    }

    /**
     * @return Valores atuais dos contadores, expostos em /mock/stats
     */
    public MockStatsResponseDTO getStats() {
        return new MockStatsResponseDTO(latencyMs, jitterMs, totalQueries.sum(), inProgress.get(), peakInProgress.get());
    }

    // Envolve a consulta com a latência e os contadores
    private <T> Mono<T> simulate(Supplier<T> query) {
        return Mono.defer(() -> {
            int current = inProgress.incrementAndGet();
            peakInProgress.accumulateAndGet(current, Math::max);
            totalQueries.increment();
            // Sorteia a variação entre -jitter e +jitter, fica 0 quando o jitter está desligado
            long variation = jitterMs > 0 ? ThreadLocalRandom.current().nextLong(-jitterMs, jitterMs + 1) : 0;
            long delay = Math.max(0, latencyMs + variation);
            // O delay agenda a resposta num temporizador, então nenhuma thread fica parada durante a espera
            return Mono.delay(Duration.ofMillis(delay))
                    .map(ignored -> query.get())
                    // Roda com sucesso, erro ou cancelamento, assim o contador nunca fica preso
                    .doFinally(signal -> inProgress.decrementAndGet());
        });
    }
}

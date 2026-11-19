package br.edu.ifba.tcc.mockdb.repositories;

import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// "Tabelas" do banco simulado, que calculam as linhas na hora a partir do id, então a mesma consulta sempre devolve o mesmo resultado
@Repository
public class MockDataRepository {

    public static final int ORDERS_PER_CUSTOMER = 10;
    public static final int ITEMS_PER_ORDER = 5;

    private static final String[] SEGMENTS = {"RETAIL", "CORPORATE", "WHOLESALE"};
    private static final String[] STATUSES = {"PAID", "SHIPPED", "DELIVERED"};

    /**
     * @param id Id do cliente
     * @return Cliente com o segmento definido pelo resto da divisão do id por 3
     */
    public CustomerDTO findCustomer(long id) {
        return new CustomerDTO(id, "Customer " + id, SEGMENTS[(int) Math.floorMod(id, 3L)]);
    }

    /**
     * @param customerId Id do cliente
     * @return 10 pedidos com ids customerId * 100 + 1 a 10 e valor entre R$ 10,00 e R$ 909,99
     */
    public List<OrderDTO> findOrders(long customerId) {
        List<OrderDTO> orders = new ArrayList<>(ORDERS_PER_CUSTOMER);
        for (int i = 1; i <= ORDERS_PER_CUSTOMER; i++) {
            long orderId = customerId * 100 + i;
            BigDecimal amount = money(1_000 + Math.floorMod(mix(orderId), 90_000L));
            orders.add(new OrderDTO(orderId, customerId, amount, STATUSES[(int) Math.floorMod(orderId, 3L)]));
        }
        return orders;
    }

    /**
     * @param orderId Id do pedido
     * @return 5 itens com SKU, quantidade (1 a 5) e preço (R$ 5,00 a R$ 204,99) tirados do hash do pedido
     */
    public List<OrderItemDTO> findItems(long orderId) {
        List<OrderItemDTO> items = new ArrayList<>(ITEMS_PER_ORDER);
        for (int i = 1; i <= ITEMS_PER_ORDER; i++) {
            long hash = mix(orderId * 31 + i);
            // Partes diferentes do mesmo hash (o >>> desloca os bits) geram valores diferentes para cada campo
            items.add(new OrderItemDTO(orderId, "SKU-" + Math.floorMod(hash, 10_000L),
                    1 + (int) Math.floorMod(hash >>> 20, 5L), money(500 + Math.floorMod(hash >>> 8, 20_000L))));
        }
        return items;
    }

    // Transforma o número sorteado em reais com 2 casas decimais (ex.: 53215 vira 532.15)
    private static BigDecimal money(long value) {
        return BigDecimal.valueOf(value, 2);
    }

    // Hash SplitMix64, funciona como um sorteio que sempre dá o mesmo resultado para o mesmo id
    private static long mix(long x) {
        long z = x + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}

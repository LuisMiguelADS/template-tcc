package br.edu.ifba.tcc.mockdb.controllers;

import br.edu.ifba.tcc.core.dtos.CustomerDTO;
import br.edu.ifba.tcc.core.dtos.OrderDTO;
import br.edu.ifba.tcc.core.dtos.OrderItemDTO;
import br.edu.ifba.tcc.mockdb.services.QuerySimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

// Endpoints chamados pelo MockDbClient das APIs, cada um equivale a uma consulta SQL num banco real
@RestController
@RequestMapping("/db")
@RequiredArgsConstructor
public class MockDbController {

    private final QuerySimulationService querySimulationService;

    /**
     * @param id Id do cliente
     * @return 200 com os dados do cliente, é a 1ª consulta da cadeia
     */
    @GetMapping("/clients/{id}")
    public Mono<ResponseEntity<CustomerDTO>> getCustomer(@PathVariable("id") long id) {
        return querySimulationService.findCustomer(id).map(ResponseEntity::ok);
    }

    /**
     * @param customerId Id do cliente
     * @return 200 com os pedidos do cliente, é a 2ª consulta da cadeia
     */
    // O segment chega na query string mas não filtra nada, só deixa a consulta dependente da anterior
    @GetMapping("/clients/{id}/orders")
    public Mono<ResponseEntity<List<OrderDTO>>> getOrders(@PathVariable("id") long customerId) {
        return querySimulationService.findOrders(customerId).map(ResponseEntity::ok);
    }

    /**
     * @param orderId Id do pedido
     * @return 200 com os itens do pedido, da 3ª consulta em diante
     */
    @GetMapping("/orders/{id}/items")
    public Mono<ResponseEntity<List<OrderItemDTO>>> getItems(@PathVariable("id") long orderId) {
        return querySimulationService.findItems(orderId).map(ResponseEntity::ok);
    }
}

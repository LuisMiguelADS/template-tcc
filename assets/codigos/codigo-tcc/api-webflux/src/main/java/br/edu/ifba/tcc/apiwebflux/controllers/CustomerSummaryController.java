package br.edu.ifba.tcc.apiwebflux.controllers;

import br.edu.ifba.tcc.apiwebflux.services.CustomerSummaryService;
import br.edu.ifba.tcc.core.dtos.CustomerSummaryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

// Endpoint que vai ser requisitado pelo k6 durante os testes de carga, onde retorna o resumo do cliente com determinado número de consultas em sequência
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class CustomerSummaryController {

    private final CustomerSummaryService customerSummaryService;

    /**
     * @param id Id do cliente, caso não seja número retorna 400
     * @param queries Quantidade de consultas realizadas ao banco, caso não
     * informado fica 3 por padrão
     * @return 200 é a resposta desejada com o resumo dos clientes, dentro de um Mono
     */
    // No service possui uma regra que se a quantidade de queries for diferente de 1 a 12 retorna 400
    @GetMapping("/{id}/summary")
    public Mono<ResponseEntity<CustomerSummaryResponseDTO>> getSummary(
            @PathVariable("id") long id,
            @RequestParam(name = "queries", defaultValue = "3") int queries) {
        return customerSummaryService.summarize(id, queries).map(ResponseEntity::ok);
    }
}

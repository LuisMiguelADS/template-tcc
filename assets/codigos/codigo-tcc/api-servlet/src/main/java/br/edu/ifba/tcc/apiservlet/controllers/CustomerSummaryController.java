package br.edu.ifba.tcc.apiservlet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifba.tcc.apiservlet.services.CustomerSummaryService;
import br.edu.ifba.tcc.core.dtos.CustomerSummaryResponseDTO;
import lombok.RequiredArgsConstructor;

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
     * @return 200 é a resposta desejada com o resumo dos clientes
     */
    // No service possui uma regra que se a quantidade de queries for diferente de 1 à 12 retorna 400
    @GetMapping("/{id}/summary")
    public ResponseEntity<CustomerSummaryResponseDTO> getSummary(
            @PathVariable("id") long id,
            @RequestParam(name = "queries", defaultValue = "3") int queries) {
        return ResponseEntity.ok(customerSummaryService.summarize(id, queries));
    }
}

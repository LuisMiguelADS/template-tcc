package br.edu.ifba.tcc.mockdb.controllers;

import br.edu.ifba.tcc.mockdb.dtos.MockStatsResponseDTO;
import br.edu.ifba.tcc.mockdb.services.QuerySimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint de estatísticas do banco simulado, usado pelos scripts para saber quando o mock subiu e para coletar o pico de consultas de cada rodada
@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
public class MockStatsController {

    private final QuerySimulationService querySimulationService;

    /**
     * @return 200 com a latência configurada, o total de consultas e o pico de consultas simultâneas
     */
    @GetMapping("/stats")
    public ResponseEntity<MockStatsResponseDTO> getStats() {
        return ResponseEntity.ok(querySimulationService.getStats());
    }
}

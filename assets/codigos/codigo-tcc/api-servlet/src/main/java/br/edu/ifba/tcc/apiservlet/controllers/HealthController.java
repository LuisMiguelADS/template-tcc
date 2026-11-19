package br.edu.ifba.tcc.apiservlet.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint para evidenciar o ambiente real de execução, importante para saber quando a API subiu e salvar a resposta nos metadados de cada rodada, como versão do Java e outros recursos desejados
@RestController
public class HealthController {

    private final String model;

    public HealthController(@Value("${tcc.experiment.model}") String model) {
        this.model = model;
    }

    /**
     * @return 200 é a resposta esperada com as informações detalhadas do
     * ambiente de execução
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Runtime runtime = Runtime.getRuntime();
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "model", model,
                "java", Runtime.version().toString(),
                // Retorna verdadeiro quando a propria requisição esta sendo executada em uma thread virtual
                "virtualRequestThread", Thread.currentThread().isVirtual(),
                // CPUs que a JVM enxerga 
                "jvmVisibleCpus", runtime.availableProcessors(),
                // Heap máximo, é a memória onde a JVM guarda os objetos criados durante a execução do programa
                "maxHeapMb", runtime.maxMemory() / (1024 * 1024)));
    }
}

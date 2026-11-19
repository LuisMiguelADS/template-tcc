package br.edu.ifba.tcc.apiwebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ponto de entrada da API reativa (Spring WebFlux sobre Netty), usa poucas threads e nenhuma delas fica parada esperando o banco
@SpringBootApplication
public class ApiWebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiWebfluxApplication.class, args);
    }
}

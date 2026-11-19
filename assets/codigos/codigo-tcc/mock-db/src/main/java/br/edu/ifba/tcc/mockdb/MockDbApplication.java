package br.edu.ifba.tcc.mockdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ponto de entrada do banco simulado, que responde as consultas das APIs com dados fixos depois de uma latência configurada, para o experimento medir só o modelo de concorrência
@SpringBootApplication
public class MockDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockDbApplication.class, args);
    }
}

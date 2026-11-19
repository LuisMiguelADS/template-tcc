package br.edu.ifba.tcc.apiservlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ponto de entrada da API bloqueante (Spring MVC sobre Tomcat), o mesmo jar roda a variante mvc (VIRTUAL_THREADS=false) e a vt (VIRTUAL_THREADS=true)
@SpringBootApplication
public class ApiServletApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiServletApplication.class, args);
    }
}

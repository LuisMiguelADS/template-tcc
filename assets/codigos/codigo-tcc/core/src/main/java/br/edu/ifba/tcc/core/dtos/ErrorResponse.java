package br.edu.ifba.tcc.core.dtos;

import java.time.LocalDateTime;

// Corpo padrão de erro das APIs, montado pelo GlobalExceptionHandler e mantido no core para as duas APIs devolverem o mesmo formato
/**
 * @param timestamp Momento em que o erro aconteceu
 * @param status Código HTTP do erro
 * @param error Descrição padrão do código HTTP
 * @param message Explicação do erro para quem chamou a API
 * @param path Rota que foi chamada
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
        ) {}

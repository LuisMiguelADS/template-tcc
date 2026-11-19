package br.edu.ifba.tcc.apiwebflux.exceptions;

import br.edu.ifba.tcc.core.dtos.ErrorResponse;
import br.edu.ifba.tcc.core.exceptions.ExternalServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;

import java.time.LocalDateTime;

// Local onde realiza a tradução dos erros que podem vir a ocorrer, os métodos são chamados quando uma exceção escapa de algum controller
@ControllerAdvice
public class GlobalExceptionHandler {

    // Monta o corpo padrão de erro com o caminho da requisição que falhou
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, ServerHttpRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getPath().value()
        );
        return ResponseEntity.status(status).body(body);
    }

    // Acionada quando a consulta está fora do escopo de 1 a 12, em relação às queries
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, ServerHttpRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Acionada quando é passado um parâmetro de um tipo diferente do esperado
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(ServerWebInputException ex, ServerHttpRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getReason(), request);
    }

    // Acionada quando o banco simulado está fora do ar, demorou mais que o timeout ou respondeu com erro
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex, ServerHttpRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }
}

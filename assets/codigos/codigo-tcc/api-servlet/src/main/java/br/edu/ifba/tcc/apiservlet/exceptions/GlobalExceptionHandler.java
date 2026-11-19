package br.edu.ifba.tcc.apiservlet.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.edu.ifba.tcc.core.dtos.ErrorResponse;
import br.edu.ifba.tcc.core.exceptions.ExternalServiceException;
import jakarta.servlet.http.HttpServletRequest;

// Local onde realiza a tradução dos erros que podem vir a ocorrer, os métodos são chamados quando uma exceção escapa de algum controller
@ControllerAdvice
public class GlobalExceptionHandler {

    // Monta o corpo padrão de erro com o caminho da requisição que falhou
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    // Acionada quando a consulta está fora do escopo de 1 à 12, em relação as queries
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Acionada quando é passado um parâmetro de um tipo diferente do esperado
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName(), request);
    }

    // Acionada quando o banco simulado está fora do ar, demorou mais que o timeout ou respondeu com erro
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }
}

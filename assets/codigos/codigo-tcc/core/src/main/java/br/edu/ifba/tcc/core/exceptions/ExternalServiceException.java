package br.edu.ifba.tcc.core.exceptions;

// Exceção lançada pelo MockDbClient quando o banco simulado falha, o GlobalExceptionHandler transforma ela em 502
public class ExternalServiceException extends RuntimeException {

    /**
     * @param message Explicação do que falhou, vai para o campo message da
     * resposta de erro
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * @param message Explicação do que falhou, vai para o campo message da
     * resposta de erro
     * @param cause Exceção original do cliente HTTP
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

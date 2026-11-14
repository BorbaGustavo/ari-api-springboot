package br.com.ari.exception;

public class IntegracaoException extends RuntimeException {

    public IntegracaoException(String message) {
        super(message);
    }

    public IntegracaoException(String message, Throwable cause) {
        super(message, cause);
    }
}

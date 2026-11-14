package br.com.ari.exception;

public class ConflitoException extends IllegalStateException {

    public ConflitoException(String message) {
        super(message);
    }

    public ConflitoException(String message, Throwable cause) {
        super(message, cause);
    }
}

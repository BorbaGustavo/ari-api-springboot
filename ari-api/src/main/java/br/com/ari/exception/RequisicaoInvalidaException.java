package br.com.ari.exception;

public class RequisicaoInvalidaException extends IllegalArgumentException {

    public RequisicaoInvalidaException(String message) {
        super(message);
    }

    public RequisicaoInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}

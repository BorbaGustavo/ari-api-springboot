package br.com.ari.exception;

public class SessaoInvalidaException extends IllegalStateException {

    public SessaoInvalidaException(String message) {
        super(message);
    }
}

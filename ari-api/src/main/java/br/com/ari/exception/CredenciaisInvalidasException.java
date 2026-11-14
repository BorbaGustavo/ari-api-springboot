package br.com.ari.exception;

public class CredenciaisInvalidasException extends IllegalArgumentException {

    public CredenciaisInvalidasException(String message) {
        super(message);
    }
}

package dev.gustavo.at.exception;

public class MedicoNaoExisteException extends RuntimeException {
    public MedicoNaoExisteException(String message) {
        super(message);
    }
}

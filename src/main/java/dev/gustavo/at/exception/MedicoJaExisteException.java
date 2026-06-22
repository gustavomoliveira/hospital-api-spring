package dev.gustavo.at.exception;

public class MedicoJaExisteException extends RuntimeException {
    public MedicoJaExisteException(String message) {
        super(message);
    }
}

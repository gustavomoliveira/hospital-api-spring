package dev.gustavo.at.exception;

public class PacienteJaExisteException extends RuntimeException {
    public PacienteJaExisteException(String message) {
        super(message);
    }
}

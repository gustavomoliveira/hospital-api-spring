package dev.gustavo.at.exception;

public class PacienteNaoExisteException extends RuntimeException {
    public PacienteNaoExisteException(String message) {
        super(message);
    }
}

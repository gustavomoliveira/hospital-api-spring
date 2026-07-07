package dev.gustavo.at.insfrastructure.exception;

import dev.gustavo.at.exception.MedicoJaExisteException;
import dev.gustavo.at.exception.MedicoNaoExisteException;
import dev.gustavo.at.exception.PacienteJaExisteException;
import dev.gustavo.at.exception.PacienteNaoExisteException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PacienteJaExisteException.class)
    public ResponseEntity<ErrorResponseDTO> handlePacienteJaExisteException(PacienteJaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(HttpStatus.CONFLICT.value(), e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(PacienteNaoExisteException.class)
    public ResponseEntity<ErrorResponseDTO> handlePacienteNaoExisteException(PacienteNaoExisteException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MedicoJaExisteException.class)
    public ResponseEntity<ErrorResponseDTO> handleMedicoJaExisteException(MedicoJaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(HttpStatus.CONFLICT.value(), e.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MedicoNaoExisteException.class)
    public ResponseEntity<ErrorResponseDTO> handleMedicoNaoExisteException(MedicoNaoExisteException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(HttpStatus.NOT_FOUND.value(), e.getMessage(), LocalDateTime.now()));
    }
}

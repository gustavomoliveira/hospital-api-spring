package dev.gustavo.at.insfrastructure.exception;

import java.time.LocalDateTime;

public record ErrorResponseDTO(Integer status, String message, LocalDateTime timeStamp) {
}

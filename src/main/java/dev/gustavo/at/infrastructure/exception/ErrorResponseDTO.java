package dev.gustavo.at.infrastructure.exception;

import java.time.LocalDateTime;

public record ErrorResponseDTO(Integer status, String message, LocalDateTime timeStamp) {
}

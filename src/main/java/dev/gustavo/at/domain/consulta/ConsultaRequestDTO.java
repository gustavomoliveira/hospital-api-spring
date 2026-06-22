package dev.gustavo.at.domain.consulta;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConsultaRequestDTO(
        @NotNull(message = "Data da consulta é obrigatória")
        LocalDate dataConsulta,

        String observacoes,

        @NotNull(message = "Paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "Médico é obrigatório")
        Long medicoId
) {
}
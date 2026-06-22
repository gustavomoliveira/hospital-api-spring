package dev.gustavo.at.domain.consulta;

import java.time.LocalDate;

public record ConsultaResponseDTO(
        Long id,
        LocalDate dataConsulta,
        String observacoes,
        Long pacienteId,
        Long medicoId
) {
}
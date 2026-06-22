package dev.gustavo.at.domain.paciente;

import java.time.LocalDate;

public record PacienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String telefone
) {
}

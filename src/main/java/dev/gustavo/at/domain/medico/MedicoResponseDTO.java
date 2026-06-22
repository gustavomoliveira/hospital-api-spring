package dev.gustavo.at.domain.medico;

public record MedicoResponseDTO(
        Long id,
        String nome,
        String crm,
        String especialidade
) {
}
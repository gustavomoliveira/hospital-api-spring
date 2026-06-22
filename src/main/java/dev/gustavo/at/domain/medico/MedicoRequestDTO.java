package dev.gustavo.at.domain.medico;

import jakarta.validation.constraints.NotBlank;

public record MedicoRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "CRM é obrigatório")
        String crm,

        @NotBlank(message = "Especialidade é obrigatória")
        String especialidade
) {
}
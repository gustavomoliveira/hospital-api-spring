package dev.gustavo.at.domain.medico;

public class MedicoMapper {

    public static Medico toEntity(MedicoRequestDTO dto) {
        return new Medico(dto.nome(), dto.crm(), dto.especialidade());
    }

    public static MedicoResponseDTO toDTO(Medico medico) {
        return new MedicoResponseDTO(
                medico.getId(),
                medico.getNome(),
                medico.getCrm(),
                medico.getEspecialidade()
        );
    }
}
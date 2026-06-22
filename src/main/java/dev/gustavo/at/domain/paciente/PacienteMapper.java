package dev.gustavo.at.domain.paciente;

public class PacienteMapper {

    public static Paciente toEntity(PacienteRequestDTO dto) {
        return new Paciente(dto.nome(), dto.cpf(), dto.dataNascimento(), dto.telefone());
    }

    public static PacienteResponseDTO toDTO(Paciente paciente) {
        return new PacienteResponseDTO(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                paciente.getTelefone()
        );
    }
}

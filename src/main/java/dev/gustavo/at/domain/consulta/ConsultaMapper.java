package dev.gustavo.at.domain.consulta;

public class ConsultaMapper {

    public static ConsultaResponseDTO toDTO(Consulta consulta) {
        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getDataConsulta(),
                consulta.getObservacoes(),
                consulta.getPaciente().getId(),
                consulta.getMedico().getId()
        );
    }
}
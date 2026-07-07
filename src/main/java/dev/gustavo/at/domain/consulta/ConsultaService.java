package dev.gustavo.at.domain.consulta;

import dev.gustavo.at.domain.medico.Medico;
import dev.gustavo.at.domain.medico.MedicoRepository;
import dev.gustavo.at.domain.paciente.Paciente;
import dev.gustavo.at.domain.paciente.PacienteRepository;
import dev.gustavo.at.exception.MedicoNaoExisteException;
import dev.gustavo.at.exception.PacienteNaoExisteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    public ConsultaResponseDTO cadastrarConsulta(ConsultaRequestDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
                .orElseThrow(() -> new PacienteNaoExisteException("Paciente não encontrado"));

        Medico medico = medicoRepository.findById(dto.medicoId())
                .orElseThrow(() -> new MedicoNaoExisteException("Médico não encontrado"));

        Consulta consulta = new Consulta(dto.dataConsulta(), dto.observacoes(), paciente, medico);
        return ConsultaMapper.toDTO(consultaRepository.save(consulta));
    }
}
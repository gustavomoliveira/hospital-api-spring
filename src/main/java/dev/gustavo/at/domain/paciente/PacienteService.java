package dev.gustavo.at.domain.paciente;

import dev.gustavo.at.exception.PacienteJaExisteException;
import dev.gustavo.at.exception.PacienteNaoExisteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteResponseDTO criarPaciente(PacienteRequestDTO dto) {
        if (pacienteRepository.existsByCpf(dto.cpf())) {
            throw new PacienteJaExisteException("Paciente já cadastrado");
        }

        Paciente paciente = PacienteMapper.toEntity(dto);
        return PacienteMapper.toDTO(pacienteRepository.save(paciente));
    }

    public PacienteResponseDTO buscarPacientePorId(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNaoExisteException("Paciente não registrado"));
        return PacienteMapper.toDTO(paciente);
    }

    public List<PacienteResponseDTO> listarPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        return pacientes.stream().map(PacienteMapper::toDTO).toList();
    }

    public void removerPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new PacienteNaoExisteException("Paciente não encontrado");
        }
        pacienteRepository.deleteById(pacienteId);
    }
}

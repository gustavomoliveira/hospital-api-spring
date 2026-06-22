package dev.gustavo.at.domain.medico;

import dev.gustavo.at.exception.MedicoJaExisteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoResponseDTO cadastrarMedico(MedicoRequestDTO dto) {
        if (medicoRepository.existsByCrm(dto.crm())) {
            throw new MedicoJaExisteException("Médico já cadastrado");
        }
        Medico medico = MedicoMapper.toEntity(dto);
        return MedicoMapper.toDTO(medicoRepository.save(medico));
    }

    public List<MedicoResponseDTO> listarMedicos() {
        return medicoRepository.findAll().stream().map(MedicoMapper::toDTO).toList();
    }

    public List<MedicoConsultasDTO> listarMedicosPorConsultas() {
        return medicoRepository.findMedicosByTotalConsultas();
    }
}
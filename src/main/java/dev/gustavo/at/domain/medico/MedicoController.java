package dev.gustavo.at.domain.medico;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService service;

    @PostMapping
    public ResponseEntity<MedicoResponseDTO> cadastrarMedico(@RequestBody MedicoRequestDTO dto) {
        MedicoResponseDTO response = service.cadastrarMedico(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponseDTO>> listarMedicos() {
        List<MedicoResponseDTO> response = service.listarMedicos();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/consultas/ranking")
    public ResponseEntity<List<MedicoConsultasDTO>> listarMedicosPorConsultas() {
        List<MedicoConsultasDTO> response = service.listarMedicosPorConsultas();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
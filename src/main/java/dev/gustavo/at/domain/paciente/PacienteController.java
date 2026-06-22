package dev.gustavo.at.domain.paciente;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> cadastrarPaciente(@RequestBody PacienteRequestDTO dto) {
        PacienteResponseDTO response = service.criarPaciente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> buscarPacientePorId(@PathVariable Long id) {
        PacienteResponseDTO response = service.buscarPacientePorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponseDTO>> listarPacientes() {
        List<PacienteResponseDTO> response = service.listarPacientes();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerPaciente(@PathVariable Long id) {
        service.removerPaciente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

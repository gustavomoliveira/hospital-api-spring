package dev.gustavo.at.domain.consulta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> cadastrarConsulta(@RequestBody @Valid ConsultaRequestDTO dto) {
        ConsultaResponseDTO response = service.cadastrarConsulta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
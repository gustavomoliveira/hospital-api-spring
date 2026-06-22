package dev.gustavo.at.insfrastructure.config;

import dev.gustavo.at.domain.medico.*;
import dev.gustavo.at.domain.paciente.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    @Override
    public void run(String... args) throws Exception {
        if (medicoRepository.count() == 0) {
            Medico cardiologista = new Medico("Carlos Lima", "12345/SP", "Cardiologia");
            Medico ortopedista = new Medico("Ana Souza", "67890/SP", "Ortopedia");
            medicoRepository.saveAll(List.of(cardiologista, ortopedista));
        }

        if (pacienteRepository.count() == 0) {
            Paciente joao = new Paciente("João Silva", "123.456.789-00", LocalDate.of(1990, 5, 20), "(11) 91234-5678");
            Paciente maria = new Paciente("Maria Oliveira", "987.654.321-00", LocalDate.of(1985, 8, 15), "(11) 99876-5432");
            pacienteRepository.saveAll(List.of(joao, maria));
        }
    }
}

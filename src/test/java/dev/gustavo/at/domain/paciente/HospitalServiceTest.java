package dev.gustavo.at.domain.paciente;

import dev.gustavo.at.domain.medico.*;
import dev.gustavo.at.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HospitalServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @InjectMocks
    private MedicoService medicoService;

    private Paciente paciente;
    private PacienteRequestDTO pacienteRequestDTO;
    private Medico medico;
    private MedicoRequestDTO medicoRequestDTO;

    @BeforeEach
    void setUp() {
        pacienteRequestDTO = new PacienteRequestDTO(
                "João Silva",
                "529.982.247-25",
                LocalDate.of(1990, 5, 20),
                "(11) 91234-5678"
        );

        paciente = new Paciente(
                "João Silva",
                "529.982.247-25",
                LocalDate.of(1990, 5, 20),
                "(11) 91234-5678"
        );

        medicoRequestDTO = new MedicoRequestDTO(
                "Carlos Lima",
                "12345/SP",
                "Cardiologia"
        );

        medico = new Medico("Carlos Lima", "12345/SP", "Cardiologia");
    }

    @Test
    void deveCadastrarPacienteComSucesso() {
        when(pacienteRepository.existsByCpf(any())).thenReturn(false);
        when(pacienteRepository.save(any())).thenReturn(paciente);

        PacienteResponseDTO response = pacienteService.criarPaciente(pacienteRequestDTO);

        assertNotNull(response);
        assertEquals("João Silva", response.nome());

        verify(pacienteRepository, times(1)).save(any());
    }

    @Test
    void deveBuscarPacientePorIdComSucesso() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        PacienteResponseDTO response = pacienteService.buscarPacientePorId(1L);

        assertNotNull(response);
        assertEquals("João Silva", response.nome());
    }

    @Test
    void deveLancarExcecaoQuandoPacienteNaoExiste() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PacienteNaoExisteException.class, () -> {
            pacienteService.buscarPacientePorId(99L);
        });
    }

    @Test
    void deveRemoverPacienteComSucesso() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pacienteRepository).deleteById(1L);

        pacienteService.removerPaciente(1L);

        verify(pacienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void deveCadastrarMedicoComSucesso() {
        when(medicoRepository.existsByCrm(any())).thenReturn(false);
        when(medicoRepository.save(any())).thenReturn(medico);

        var response = medicoService.cadastrarMedico(medicoRequestDTO);

        assertNotNull(response);
        assertEquals("Carlos Lima", response.nome());
        verify(medicoRepository, times(1)).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoMedicoJaExiste() {
        when(medicoRepository.existsByCrm(any())).thenReturn(true);

        assertThrows(MedicoJaExisteException.class, () -> {
            medicoService.cadastrarMedico(medicoRequestDTO);
        });
    }
}

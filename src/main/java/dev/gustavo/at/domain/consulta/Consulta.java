package dev.gustavo.at.domain.consulta;

import dev.gustavo.at.domain.medico.Medico;
import dev.gustavo.at.domain.paciente.Paciente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataConsulta;

    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    public Consulta(LocalDate dataConsulta, String observacoes, Paciente paciente, Medico medico) {
        this.dataConsulta = dataConsulta;
        this.observacoes = observacoes;
        this.paciente = paciente;
        this.medico = medico;
    }
}

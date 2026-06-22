package dev.gustavo.at.domain.internacao;

import dev.gustavo.at.domain.paciente.Paciente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "internacoes")
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dataEntrada;

    @Column(nullable = false)
    private LocalDate dataAlta;

    @Column(nullable = false)
    private String quarto;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

}

package dev.gustavo.at.domain.medico;

import dev.gustavo.at.domain.consulta.Consulta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "medicos")
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String crm;

    @Column(nullable = false)
    private String especialidade;

    @OneToMany(mappedBy = "medico")
    private List<Consulta> consultas;

    public Medico(String nome, String crm, String especialidade) {
        this.nome = nome;
        this.crm = crm;
        this.especialidade = especialidade;
    }
}

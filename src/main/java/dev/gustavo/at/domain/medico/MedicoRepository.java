package dev.gustavo.at.domain.medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    boolean existsByCrm(String crm);

    @Query("SELECT new dev.gustavo.at.domain.medico.MedicoConsultasDTO(m.nome, COUNT(c)) " +
            "FROM Medico m LEFT JOIN m.consultas c " +
            "GROUP BY m.nome " +
            "ORDER BY COUNT(c) DESC")
    List<MedicoConsultasDTO> findMedicosByTotalConsultas();
}

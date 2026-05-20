package org.sid.pfespring.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.Soutenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {
    @Modifying
    @Query("DELETE FROM Soutenance s WHERE s.version = :version")
    void deleteSoutenancesByVersion(@Param("version") ImportVersion version);
    /**
     * Utilisé pour l'export Excel : toutes les soutenances triées
     * par date, puis heure de début, puis nom de salle.
     */
    List<Soutenance> findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(ImportVersion version);


}
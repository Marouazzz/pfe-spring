package org.sid.pfespring.repository;

import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.Soutenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {

    //Contrainte 1 : chevauchement de salle
    // Vérifie si  salle est déjà occupée sur un créneau
    boolean existsBySalleAndDateSoutenanceAndHeureDebutLessThanAndHeureFinGreaterThan(
            Salle salle,
            LocalDate dateSoutenance,
            LocalTime heureFin,
            LocalTime heureDebut
    );

    // Contrainte 2 : un prof ne peut pas être dans 2 jurys simultanément
    @Query("""
        SELECT COUNT(s) > 0 FROM Soutenance s
        WHERE s.dateSoutenance = :date
          AND s.heureDebut < :heureFin
          AND s.heureFin   > :heureDebut
          AND (
               s.jury.encadrant.id = :profId
            OR s.jury.prof1.id          = :profId
            OR s.jury.prof2.id          = :profId
          )
    """)
    boolean existsProfConflict(
            @Param("date")       LocalDate date,
            @Param("heureDebut") LocalTime heureDebut,
            @Param("heureFin")   LocalTime heureFin,
            @Param("profId")     Long profId
    );

    //Contrainte 3 : répartition équilibrée par jour par prof
    @Query("""
        SELECT COUNT(s) FROM Soutenance s
        WHERE s.dateSoutenance = :date
          AND (
               s.jury.encadrant.id = :profId
            OR s.jury.prof1.id          = :profId
            OR s.jury.prof2.id          = :profId
          )
    """)
    long countByProfIdAndDate(
            @Param("profId") Long profId,
            @Param("date")   LocalDate date
    );

    //  Pour l'export
    List<Soutenance> findAllByOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc();
}
package org.sid.pfespring.repository;

import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.Soutenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════════════════
 *  SoutenanceRepository
 *
 *  NOTE : Avec le nouvel algorithme en mémoire, TOUTES les vérifications
 *  de contraintes (chevauchement, battement, quotas) se font en mémoire
 *  dans SalleServiceImpl via la liste planningMemoire.
 *
 *  Ce repository ne sert plus qu'à :
 *  - deleteAll()    → nettoyage avant relance
 *  - save()         → sauvegarde finale des slots planifiés
 *  - findAll...()   → export Excel
 *
 *  Les méthodes @Query complexes (existsProfConflict, hasProfAdjacentSoutenance,
 *  countByProfIdAndDate, countTotalByProfId) ne sont PLUS utilisées par
 *  l'algorithme principal. Elles sont conservées ici en commentaires
 *  pour référence ou usage futur (ex: validation post-planification).
 * ════════════════════════════════════════════════════════════════════
 */
@Repository
public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {
    @Modifying
    @Query("DELETE FROM Soutenance s")
    void deleteAllSoutenances();
    /**
     * Utilisé pour l'export Excel : toutes les soutenances triées
     * par date, puis heure de début, puis nom de salle.
     */
    List<Soutenance> findAllByOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc();

    /**
     * Conservé pour compatibilité (utilisé dans l'ancienne version).
     * Plus appelé par l'algorithme principal mais peut servir
     * à une validation post-planification ou à des tests.
     */
    boolean existsBySalleAndDateSoutenanceAndHeureDebutLessThanAndHeureFinGreaterThan(
            Salle     salle,
            LocalDate dateSoutenance,
            LocalTime heureDebut,
            LocalTime heureFin
    );
}
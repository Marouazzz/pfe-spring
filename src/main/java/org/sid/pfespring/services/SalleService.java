package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

public interface SalleService extends GenericService<RequestSalleDTO, ResponseSalleDTO> {

    /**
     * Importe les salles depuis la feuille "salles" du fichier Excel.
     * Appelé lors de l'upload initial — idempotent (ignore les doublons).
     */
    List<ResponseSalleDTO> importFromExcel(InputStream inputStream) throws Exception;

    /**
     * Lit la date de début dans la feuille "jours_soutenances".
     * Une seule date suffit — l'algo calcule les jours ouvrés suivants.
     */
    LocalDate importDateDebut(InputStream inputStream) throws Exception;

    /**
     * Lance la planification à partir d'une date de début.
     * L'algo génère automatiquement les jours ouvrés nécessaires
     * pour caser 75 soutenances (pas de weekend).
     */
    List<ResponseSoutenanceDTO> affecterSalles(LocalDate dateDebut);

    /** Génère le fichier Excel du planning trié. */
    byte[] exportPlanningExcel() throws Exception;
}
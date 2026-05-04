package org.sid.pfespring.services;

import java.time.LocalDate;
import java.util.List;
import org.sid.pfespring.model.ImportVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;

public interface SalleService extends GenericService<RequestSalleDTO, ResponseSalleDTO> {

    /**
     * Importe les salles depuis la feuille "salles" du fichier Excel.
     * Appelé lors de l'upload initial — idempotent (ignore les doublons).
     */
    List<ResponseSalleDTO> importFromExcel(Sheet sheet,ImportVersion version) throws Exception;

    /**
     * Lit la date de début dans la feuille "jours_soutenances".
     * Une seule date suffit — l'algo calcule les jours ouvrés suivants.
     */
    LocalDate importDateDebut(Sheet sheet) throws Exception;

    /**
     * Lance la planification à partir d'une date de début.
     * L'algo génère automatiquement les jours ouvrés nécessaires
     * pour caser 75 soutenances (pas de weekend).
     */
    List<ResponseSoutenanceDTO> affecterSalles(LocalDate dateDebut,Long versionId);

    /** Génère le fichier Excel du planning trié. */
    byte[] exportPlanningExcel(Long versionId) throws Exception;
}
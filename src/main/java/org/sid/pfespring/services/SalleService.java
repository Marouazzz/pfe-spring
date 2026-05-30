package org.sid.pfespring.services;

import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.model.ImportVersion;
public interface SalleService{

    /**
     * Importe les salles depuis la feuille "salles" du fichier Excel.
     * Appelé lors de l'upload initial — idempotent (ignore les doublons).
     */
    List<ResponseSalleDTO> importFromExcel(Sheet sheet, ImportVersion version);

    /**
     * Lit la date de début dans la feuille "jours_soutenances".
     * Une seule date suffit — l'algo calcule les jours ouvrés suivants.
     */
    LocalDate importDateDebut(Sheet sheet);
}
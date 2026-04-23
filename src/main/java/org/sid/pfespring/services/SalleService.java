package org.sid.pfespring.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

public interface SalleService extends GenericService<RequestSalleDTO, ResponseSalleDTO> {

    // Import des salles depuis la sheet "salles" du Excel
    List<ResponseSalleDTO> importFromExcel(InputStream inputStream) throws Exception;

    // Import des jours depuis la sheet "jours_soutenances" du Excel
    List<LocalDate> importJoursSoutenances(InputStream inputStream) throws Exception;

    // Lance l'algorithme d'affectation et sauvegarde en BDD
    List<ResponseSoutenanceDTO> affecterSalles(List<LocalDate> jours);

    // Génère le fichier Excel du planning complet
    byte[] exportPlanningExcel() throws Exception;
}
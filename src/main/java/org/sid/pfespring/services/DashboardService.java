package org.sid.pfespring.services;

import java.util.List;
import java.util.Map;

import org.sid.pfespring.model.scheduling.SchedulingSolution;

public interface DashboardService {

    /** Nombre de pfe encadrés par professeur → [{nom, count}] */
    public List<Map<String, Object>> pfesParEncadrant(Long versionId);

    /** Nombre de soutenances par professeur (encadrant + jurys) → [{nom, count}] */
    List<Map<String, Object>> soutenancesParProf(Long versionId);

    /** Nombre de soutenances par filière → [{filiere, count}] */
    List<Map<String, Object>> soutenancesParFiliere(SchedulingSolution sol,Long versionId) ;

    /** Stats globales : total étudiants, profs, soutenances, salles */
    Map<String, Object> statsGlobales(SchedulingSolution sol,Long versionId);

    /** Anomalies affectation encadrants : profs trop chargés ou sous-chargés */
    List<Map<String, Object>> anomaliesEncadrement(Long versionId);

    List<String> detecterAnomaliesPlanning(SchedulingSolution sol,Long versionId);


}
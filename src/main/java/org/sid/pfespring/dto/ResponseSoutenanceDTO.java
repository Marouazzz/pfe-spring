// ─── ResponseSoutenanceDTO.java ───────────────────────────────────────────────
package org.sid.pfespring.dto;
import java.time.LocalDate;

public record ResponseSoutenanceDTO(
        Long       id,
        String     sujetPFE,
        String     etudiants,
        String     encadrant,
        String     prof1,
        String     prof2,
        ResponseSalleDTO salle,
        LocalDate  dateSoutenance,
        String     heureDebut,
        String     heureFin
) {}

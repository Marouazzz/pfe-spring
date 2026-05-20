package org.sid.pfespring.dto;

public record ResponseJuryDTO(
        Long id,
        String sujetPFE,
        ResponseEtudiantDTO etudiant,
        ResponseProfDTO encadrant,
        ResponseProfDTO prof1,
        ResponseProfDTO prof2   // null si aucun prof dispo
) {}

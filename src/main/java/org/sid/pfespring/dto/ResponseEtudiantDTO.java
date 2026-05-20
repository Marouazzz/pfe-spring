package org.sid.pfespring.dto;
import org.sid.pfespring.model.Filiere;

public record ResponseEtudiantDTO(String cne,String nom,String prenom,Filiere filiere){}
package org.sid.pfespring.dto;
import org.sid.pfespring.model.Specialite;

public record ResponseProfDTO(Long id,String nom,String prenom,Specialite specialite,Integer maxEtudiants) {}

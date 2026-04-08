package org.sid.pfespring.dto;
import org.sid.pfespring.model.Status;

public record ResponsePFEDTO (String sujet,String description,Status status,ResponseEtudiantDTO etudiant,ResponseProfDTO prof){}

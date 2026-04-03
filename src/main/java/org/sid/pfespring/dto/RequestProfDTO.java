package org.sid.pfespring.dto;


import lombok.Getter;
import lombok.Setter;
/*
@Getter
@Setter
public class RequestProfDTO {
    private String nom;
    private String prenom;
    private String specialite;
    private int maxEtudiants;
    private String password;

}*/

public record RequestProfDTO(
        String nom,
        String prenom,
        String specialite,
        Integer maxEtudiants
) {}
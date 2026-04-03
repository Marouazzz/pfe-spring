package org.sid.pfespring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
/*
@Getter
@Setter
public class RequestEtudiantDTO {
    private String cne;
    private String nom;
    private String prenom;
    private String filiere;
    private String password;
}*/
//avec records


public record RequestEtudiantDTO(
        String cne,
        String nom,
        String prenom,
        String filiere
) {}

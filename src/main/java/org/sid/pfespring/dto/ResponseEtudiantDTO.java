package org.sid.pfespring.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseEtudiantDTO {

    private String cne;
    private String nom;
    private String prenom;
    private String filiere;
    private String role = "ETUDIANT";
}

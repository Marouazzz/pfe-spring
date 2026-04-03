package org.sid.pfespring.dto;
import lombok.Getter;
import lombok.Setter;
import org.sid.pfespring.model.Filiere;
/*
@Getter
@Setter
public class ResponseEtudiantDTO {

    private String cne;
    private String nom;
    private String prenom;
    private String filiere;
    private String role = "ETUDIANT";
}
*/
//version record
import org.sid.pfespring.model.Filiere;

public record ResponseEtudiantDTO(

        String cne,
        String nom,
        String prenom,
        Filiere filiere
){}
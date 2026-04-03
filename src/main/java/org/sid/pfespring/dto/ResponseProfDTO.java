package org.sid.pfespring.dto;
import lombok.Getter;
import lombok.Setter;
import org.sid.pfespring.model.Specialite;
/*
@Getter
@Setter
public class ResponseProfDTO {
    private Long id;
    private String nom;
    private String prenom;
    private Specialite specialite;
    private int maxEtudiants;
    private String role = "PROF";
}*/
//version record
public record ResponseProfDTO(
        Long id,
        String nom,
        String prenom,
        Specialite specialite,
        Integer maxEtudiants
) {}

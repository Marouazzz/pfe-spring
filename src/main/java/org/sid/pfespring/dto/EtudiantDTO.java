package org.sid.pfespring.dto;


import lombok.Data;
import org.sid.pfespring.model.Filiere;

public class EtudiantDTO {

    // DTO utilisé en réponse API
    @Data
    public static class Response {
        private Long id;
        private String cne;
        private String nom;
        private String prenom;
        private Filiere filiere;
    }

    // DTO utilisé pour création
    @Data
    public static class Request {
        private String cne;
        private String nom;
        private String prenom;
        private String filiere;
    }
}
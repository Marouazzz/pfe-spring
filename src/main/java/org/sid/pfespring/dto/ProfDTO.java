package org.sid.pfespring.dto;

import lombok.Data;
import org.sid.pfespring.model.Specialite;

public class ProfDTO {
@Data
    public static class Response{
    private Long id;
    private String nom;
    private String prenom;
    private Specialite specialite;
}
    @Data
    public static class Request{

        private String nom;
        private String prenom;
        private String specialite; // type depuis excel
    }



}

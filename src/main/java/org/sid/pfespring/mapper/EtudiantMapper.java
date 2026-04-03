package org.sid.pfespring.mapper;

import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.Filiere;
import org.springframework.stereotype.Component;


@Component
public class EtudiantMapper extends AbstractMapper<Etudiant, RequestEtudiantDTO, ResponseEtudiantDTO> {

    @Override
    public Etudiant toEntity(RequestEtudiantDTO request) {
        return Etudiant.builder()
                .cne(request.getCne())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .filiere(Filiere.valueOf(
                        request.getFiliere().trim().toUpperCase()
                ))
                .build();
    }

    @Override
    public ResponseEtudiantDTO toResponse(Etudiant etudiant) {
        ResponseEtudiantDTO response = new ResponseEtudiantDTO();

        response.setCne(etudiant.getCne());
        response.setNom(etudiant.getNom());
        response.setPrenom(etudiant.getPrenom());
        response.setFiliere(etudiant.getFiliere().name());
        return response;
    }
}
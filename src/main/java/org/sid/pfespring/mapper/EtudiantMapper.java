package org.sid.pfespring.mapper;

import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.Filiere;
import org.springframework.stereotype.Component;

@Component
public class EtudiantMapper
        extends AbstractMapper<Etudiant, RequestEtudiantDTO, ResponseEtudiantDTO> {

    @Override
    public Etudiant toEntity(RequestEtudiantDTO request) {
        return Etudiant.builder()
                .cne(request.cne())
                .nom(request.nom())
                .prenom(request.prenom())
                .filiere(Filiere.valueOf(
                        request.filiere().trim().toUpperCase()
                ))
                .build();
    }

    @Override
    public ResponseEtudiantDTO toResponse(Etudiant etudiant) {
        return new ResponseEtudiantDTO(
                etudiant.getCne(),
                etudiant.getNom(),
                etudiant.getPrenom(),
                etudiant.getFiliere()
        );
    }
}
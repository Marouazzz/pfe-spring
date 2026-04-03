package org.sid.pfespring.mapper;

import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.springframework.stereotype.Component;

@Component
public class ProfMapper extends AbstractMapper<Prof, RequestProfDTO, ResponseProfDTO> {

    @Override
    public Prof toEntity(RequestProfDTO request) {
        return Prof.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .specialite(Specialite.valueOf(
                        request.getSpecialite().trim().toUpperCase()
                ))
                .maxEtudiants(request.getMaxEtudiants())
                .build();
    }

    @Override
    public ResponseProfDTO toResponse(Prof prof) {
        ResponseProfDTO response = new ResponseProfDTO();
        response.setId(prof.getId());
        response.setNom(prof.getNom());
        response.setPrenom(prof.getPrenom());
        response.setSpecialite(prof.getSpecialite());
        response.setMaxEtudiants(prof.getMaxEtudiants());
        return response;
    }
}
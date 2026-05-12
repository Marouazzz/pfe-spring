package org.sid.pfespring.mapper;

import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.springframework.stereotype.Component;

@Component
public class ProfMapper implements GenericMapper<Prof, RequestProfDTO, ResponseProfDTO> {

    @Override
    public Prof toEntity(RequestProfDTO request) {
        return Prof.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .specialite(Specialite.valueOf(
                        request.specialite().trim().toUpperCase()
                ))
                .version(request.version())
                .build();
    }

    @Override
    public ResponseProfDTO toResponse(Prof prof) {
        return new ResponseProfDTO(
                prof.getId(),
                prof.getNom(),
                prof.getPrenom(),
                prof.getSpecialite()
        );
    }
}
package org.sid.pfespring.mapper;

import org.sid.pfespring.dto.RequestJuryDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.dto.ResponseJuryDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.model.Jury;
import org.springframework.stereotype.Component;

@Component
public class JuryMapper implements GenericMapper<Jury, RequestJuryDTO, ResponseJuryDTO> {

    private final EtudiantMapper em;
    private final ProfMapper pm;

    public JuryMapper(EtudiantMapper etudiantMapper, ProfMapper profMapper) {
        this.em = etudiantMapper;
        this.pm = profMapper;
    }

    // Non utilised—
    @Override
    public Jury toEntity(RequestJuryDTO request) {
        return Jury.builder().build();
    }

    @Override
    public ResponseJuryDTO toResponse(Jury jury) {
        // Prendre le premier étudiant du Set du PFE
        ResponseEtudiantDTO etudiant = jury.getPfe().getEtudiants().stream()
                .findFirst()
                .map(em::toResponse)
                .orElse(null);

        return new ResponseJuryDTO(
                jury.getId(),
                jury.getPfe().getSujet(),
                etudiant,
                pm.toResponse(jury.getEncadrant()),
                pm.toResponse(jury.getProf1()),
                jury.getProf2() != null ? pm.toResponse(jury.getProf2()) : null
        );
    }
}

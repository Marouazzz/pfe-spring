package org.sid.pfespring.mapper;

import lombok.RequiredArgsConstructor;
import org.sid.pfespring.dto.RequestSoutenanceDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;
import org.sid.pfespring.model.Soutenance;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SoutenanceMapper implements GenericMapper<Soutenance, RequestSoutenanceDTO, ResponseSoutenanceDTO> {

    private final SalleMapper salleMapper;

    @Override
    public Soutenance toEntity(RequestSoutenanceDTO dto) {
        return Soutenance.builder().build();
    }

    @Override
    public ResponseSoutenanceDTO toResponse(Soutenance s) {
        if (s == null) return null;

        String etudiants = s.getPfe().getEtudiants().stream()
                .map(e -> e.getNom() + " " + e.getPrenom())
                .collect(Collectors.joining(", "));

        String prof2 = (s.getJury().getProf2() != null)
                ? s.getJury().getProf2().getNom() + " " + s.getJury().getProf2().getPrenom()
                : "N/A";

        return new ResponseSoutenanceDTO(
                s.getId(),
                s.getPfe().getSujet(),
                etudiants,
                s.getJury().getEncadrant().getNom() + " " + s.getJury().getEncadrant().getPrenom(),
                s.getJury().getProf1().getNom() + " " + s.getJury().getProf1().getPrenom(),
                prof2,
                salleMapper.toResponse(s.getSalle()),
                s.getDateSoutenance(),
                s.getHeureDebut().toString(),
                s.getHeureFin().toString()
        );
    }
}

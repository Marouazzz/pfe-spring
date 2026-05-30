package org.sid.pfespring.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.exception.NotSupportedLanguageException;
import org.sid.pfespring.model.PFE;
import org.springframework.stereotype.Component;



@Component
public class PFEMapper implements GenericMapper<PFE,RequestPFEDTO, ResponsePFEDTO> {

    private EtudiantMapper em;
    private ProfMapper pm;

    public PFEMapper(EtudiantMapper etudiantMapper,ProfMapper profMapper){
        this.em = etudiantMapper;
        this.pm = profMapper;
    }

    @Override
    public PFE toEntity(RequestPFEDTO dto) {
        if (dto.langue() == null || dto.langue().isBlank()) {
            throw  new NotSupportedLanguageException( "La langue du PFE n'est pas prise en charge.");
        }
        return PFE.builder().sujet(dto.sujet()).filiere(dto.filiere()).langue(dto.langue()).build();
    }

    @Override
    public ResponsePFEDTO toResponse(PFE entity) {
        Set<ResponseEtudiantDTO> etudiants = entity.getEtudiants()
        .stream()
        .map(this.em::toResponse)
        .collect(Collectors.toSet());

        if(entity.getEncadrant() != null){
            ResponseProfDTO prof =  this.pm.toResponse(entity.getEncadrant().getProf());
            // This line is wrong logically, fix it later
            return new ResponsePFEDTO(entity.getSujet(),entity.getStatus(),etudiants, prof);
        }else{
            return new ResponsePFEDTO(entity.getSujet(), entity.getStatus(), etudiants, null);
        }
    }

}
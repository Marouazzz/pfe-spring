package org.sid.pfespring.mapper;

import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.mapper.AbstractMapper;
import org.sid.pfespring.model.Salle;
import org.springframework.stereotype.Component;

@Component
// L'ordre doit être : Salle (E), RequestSalleDTO (Req), ResponseSalleDTO (Res)
public class SalleMapper extends AbstractMapper<Salle, RequestSalleDTO, ResponseSalleDTO> {

    @Override
    public Salle toEntity(RequestSalleDTO dto) {
        if (dto == null) return null;
        return Salle.builder()
                .nomSalle(dto.nomSalle())
                .capacite(dto.capacite())
                .disponible(true)
                .build();
    }

    @Override
    public ResponseSalleDTO toResponse(Salle entity) {
        if (entity == null) return null;
        return new ResponseSalleDTO(
                entity.getId(),
                entity.getNomSalle(),
                entity.getCapacite(),
                entity.isDisponible()
        );
    }
}

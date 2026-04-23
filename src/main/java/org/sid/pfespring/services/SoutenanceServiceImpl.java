package org.sid.pfespring.services;

import lombok.RequiredArgsConstructor;
import org.sid.pfespring.dto.RequestSoutenanceDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;
import org.sid.pfespring.mapper.SoutenanceMapper;
import org.sid.pfespring.model.Soutenance;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SoutenanceServiceImpl
        // Correction de l'ordre : Soutenance (E), RequestSoutenanceDTO (Req), ResponseSoutenanceDTO (Res)
        extends AbstractService<Soutenance, RequestSoutenanceDTO, ResponseSoutenanceDTO>
        implements SoutenanceService {

    private final SoutenanceRepository soutenanceRepository;
    private final SoutenanceMapper soutenanceMapper;

    // Constructeur manuel pour injecter dans le super et dans la classe
    public SoutenanceServiceImpl(SoutenanceRepository soutenanceRepository,
                                 SoutenanceMapper soutenanceMapper) {
        super(soutenanceRepository, soutenanceMapper);
        this.soutenanceRepository = soutenanceRepository;
        this.soutenanceMapper = soutenanceMapper;
    }
    @Override
    public List<ResponseSoutenanceDTO> findByDate(LocalDate date) {
        return soutenanceRepository.findAll().stream()
                .filter(s -> s.getDateSoutenance().equals(date))
                .map(soutenanceMapper::toResponse)
                .toList();
    }

    @Override
    public List<ResponseSoutenanceDTO> findBySalle(Long salleId) {
        return soutenanceRepository.findAll().stream()
                .filter(s -> s.getSalle().getId().equals(salleId))
                .map(soutenanceMapper::toResponse)
                .toList();
    }

    @Override
    public List<ResponseSoutenanceDTO> findByProfId(Long profId) {
        return soutenanceRepository.findAll().stream()
                .filter(s ->
                        s.getJury().getEncadrant().getId().equals(profId)
                                || s.getJury().getProf1().getId().equals(profId)
                                || s.getJury().getProf2().getId().equals(profId)
                )
                .map(soutenanceMapper::toResponse)
                .toList();
    }
}
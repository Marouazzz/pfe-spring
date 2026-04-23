package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestSoutenanceDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;

import java.time.LocalDate;
import java.util.List;

public interface SoutenanceService extends GenericService<RequestSoutenanceDTO, ResponseSoutenanceDTO> {

    List<ResponseSoutenanceDTO> findByDate(LocalDate date);
    List<ResponseSoutenanceDTO> findBySalle(Long salleId);
    List<ResponseSoutenanceDTO> findByProfId(Long profId);
}
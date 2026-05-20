package org.sid.pfespring.repository;

import java.util.List;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SalleRepository extends JpaRepository<Salle, Long> {
    List<Salle> findByVersionAndDisponibleTrue(ImportVersion version);
  //  <Salle> findByNomSalle(String nomSalle);
    boolean existsByNomSalleAndVersion(String nomSalle,ImportVersion version);
}
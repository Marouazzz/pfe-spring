package org.sid.pfespring.repository;

import org.sid.pfespring.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface SalleRepository extends JpaRepository<Salle, Long> {
    List<Salle> findByDisponibleTrue();
  //  <Salle> findByNomSalle(String nomSalle);
    boolean existsByNomSalle(String nomSalle);
}
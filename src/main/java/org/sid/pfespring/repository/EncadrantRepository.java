package org.sid.pfespring.repository;

import org.sid.pfespring.model.Encadrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EncadrantRepository extends JpaRepository<Encadrant, Long>{
}

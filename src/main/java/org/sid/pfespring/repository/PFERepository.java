package org.sid.pfespring.repository;

import org.sid.pfespring.model.PFE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PFERepository extends JpaRepository<PFE,Long> {}

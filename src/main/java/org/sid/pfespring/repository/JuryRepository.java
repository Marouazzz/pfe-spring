package org.sid.pfespring.repository;

import org.sid.pfespring.model.Jury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JuryRepository extends JpaRepository<Jury, Long> {}

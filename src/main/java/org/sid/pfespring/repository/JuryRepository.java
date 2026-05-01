package org.sid.pfespring.repository;

import java.util.List;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JuryRepository extends JpaRepository<Jury, Long> {
    List<Jury> findByVersion(ImportVersion version);
}

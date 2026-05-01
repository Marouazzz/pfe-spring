package org.sid.pfespring.repository;

import org.sid.pfespring.model.PFE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.sid.pfespring.model.ImportVersion;

@Repository
public interface PFERepository extends JpaRepository<PFE,Long> {
    List<PFE> findByVersion(ImportVersion version);
}

package org.sid.pfespring.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import jakarta.transaction.Transactional;
import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.ImportVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface EncadrantRepository extends JpaRepository<Encadrant, Long>{
    List<Encadrant> findByVersion(ImportVersion version);
    @Transactional
    void deleteByVersion(ImportVersion version);

    @Query("SELECT DISTINCT e FROM Encadrant e LEFT JOIN FETCH e.pfes WHERE e.version = :version")
    List<Encadrant> findByVersionWithPfes(@Param("version") ImportVersion version);
}

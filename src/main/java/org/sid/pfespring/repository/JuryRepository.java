package org.sid.pfespring.repository;

import java.util.List;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JuryRepository extends JpaRepository<Jury, Long> {
    List<Jury> findByVersion(ImportVersion version);

    @Modifying
    @Transactional
    @Query("DELETE FROM Jury j WHERE j.version = :version")
    void deleteByVersionJpql(@Param("version") ImportVersion version);

    // JuryRepository.java
    @Query("SELECT DISTINCT j FROM Jury j " +
            "LEFT JOIN FETCH j.encadrant " +
            "LEFT JOIN FETCH j.prof1 " +
            "LEFT JOIN FETCH j.prof2 " +
            "LEFT JOIN FETCH j.pfe "+
            "WHERE j.version = :version")
    List<Jury> findAllWithRelations(@Param("version") ImportVersion version);
}

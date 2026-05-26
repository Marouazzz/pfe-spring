package org.sid.pfespring.repository;


import java.util.List;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.PFE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface PFERepository extends JpaRepository<PFE,Long> {
    List<PFE> findByVersion(ImportVersion version);
    @Modifying
    @Transactional
    @Query("UPDATE pfes p SET p.encadrant = null WHERE p.version = :version")
    void clearEncadrantByVersion(@Param("version") ImportVersion version);
}

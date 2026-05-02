package org.sid.pfespring.repository;

import org.sid.pfespring.model.Jury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JuryRepository extends JpaRepository<Jury, Long> {
    // JuryRepository.java
    @Query("SELECT j FROM Jury j " +
            "LEFT JOIN FETCH j.encadrant " +
            "LEFT JOIN FETCH j.prof1 " +
            "LEFT JOIN FETCH j.prof2 " +
            "LEFT JOIN FETCH j.pfe")
    List<Jury> findAllWithRelations();
}

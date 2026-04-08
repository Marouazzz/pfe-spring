package org.sid.pfespring.repository;

import java.util.List;

import org.sid.pfespring.model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {

    @Query("SELECT e.cne FROM Etudiant e WHERE e.cne IN :cnes")
    List<String> findExistingCNE(@Param("cnes") List<String> cnes);

    List<Etudiant> findByCneIn(List<String> cnes);
}
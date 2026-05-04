package org.sid.pfespring.repository;


import java.util.List;

import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sid.pfespring.model.ImportVersion;

@Repository
public interface ProfRepository extends JpaRepository<Prof,Long> {
    // Utilisation de convention de nommage 
    public List<Prof> findByVersionAndSpecialiteNotIn(ImportVersion version,List<Specialite> specialites);
    public List<Prof> findByVersionAndSpecialite(ImportVersion version,Specialite specialite);
}

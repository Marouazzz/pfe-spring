package org.sid.pfespring.repository;

import org.sid.pfespring.model.ImportVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImportVersionRepository extends JpaRepository<ImportVersion,Long> {
    ImportVersion findTopByOrderByIdDesc();
}
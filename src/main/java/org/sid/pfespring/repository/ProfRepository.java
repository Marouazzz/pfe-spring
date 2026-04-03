package org.sid.pfespring.repository;


import org.sid.pfespring.model.Prof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfRepository extends JpaRepository<Prof,Long> {

}

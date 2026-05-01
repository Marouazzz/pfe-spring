package org.sid.pfespring.dto;

import org.sid.pfespring.model.ImportVersion;


public record RequestEtudiantDTO(String cne,String nom,String prenom,String filiere,ImportVersion version) {}

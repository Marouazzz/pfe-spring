package org.sid.pfespring.dto;

import org.sid.pfespring.model.ImportVersion;

public record RequestProfDTO(String nom,String prenom,String specialite,ImportVersion version) {}
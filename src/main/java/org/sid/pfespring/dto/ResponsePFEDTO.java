package org.sid.pfespring.dto;
import java.util.Set;

import org.sid.pfespring.model.Status;

public record ResponsePFEDTO (String sujet,String description,Status status,Set<ResponseEtudiantDTO> etudiants,ResponseProfDTO prof){}

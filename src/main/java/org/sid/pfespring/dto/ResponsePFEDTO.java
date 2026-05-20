package org.sid.pfespring.dto;
import java.util.Set;

import org.sid.pfespring.model.Status;

public record ResponsePFEDTO (String sujet,Status status,Set<ResponseEtudiantDTO> etudiants,ResponseProfDTO prof){}

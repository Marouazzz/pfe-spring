package org.sid.pfespring.dto;

import org.sid.pfespring.model.ImportVersion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestProfDTO(
@NotBlank(message="Le nom est obligatoire") String nom,
@NotBlank(message="Le prenom est obligatoire") String prenom,
@NotBlank(message="La specialite est obligatoire") String specialite,
@NotNull ImportVersion version) {}
package org.sid.pfespring.dto;

import org.sid.pfespring.model.ImportVersion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestProfDTO(@NotBlank String nom,@NotBlank String prenom,@NotBlank String specialite,@NotNull ImportVersion version) {}
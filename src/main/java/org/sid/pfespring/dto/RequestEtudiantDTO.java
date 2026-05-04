package org.sid.pfespring.dto;

import org.sid.pfespring.model.ImportVersion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record RequestEtudiantDTO(@NotBlank @Pattern(regexp="^[A-Z][1-9][0-9]{8}") String cne,@NotBlank String nom,@NotBlank String prenom,@NotBlank String filiere,@NotNull ImportVersion version) {}

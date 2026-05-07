package org.sid.pfespring.dto;

import org.sid.pfespring.model.ImportVersion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record RequestEtudiantDTO(
@NotBlank(message="Le CNE est obligatoire ") @Pattern(regexp="^[A-Z][1-9][0-9]{8}",message="Le CNE doit respecter la formuler suivant AYXXXXXXXXX \n A : un alphabet majuscule [A-Z] \n Y : Chiffre compris entre 1 et 9\n X : Chiffre compris entre 0 et 9") String cne,
@NotBlank(message="Le nom est obligatoire") String nom,
@NotBlank(message="Le prenom est obligatoire") String prenom,
@NotBlank(message="La filiere est obligatoire ") String filiere,
@NotNull ImportVersion version
) {}

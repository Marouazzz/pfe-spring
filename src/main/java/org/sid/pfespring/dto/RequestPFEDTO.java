package org.sid.pfespring.dto;

import java.util.ArrayList;
import java.util.Set;

import org.sid.pfespring.model.Filiere;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RequestPFEDTO(
Set<@NotBlank(message="Le CNE est obligatoire ") @Pattern(regexp="^[A-Z][1-9][0-9]{8}",message="Le CNE doit respecter la formuler suivant AYXXXXXXXXX \n A : un alphabet majuscule [A-Z] \n Y : Chiffre compris entre 1 et 9\n X : Chiffre compris entre 0 et 9") String> cnes,
@NotBlank(message="Le sujet est Obligatoire") String sujet,
@NotNull(message="Le Filiere est obligatoire (TDIA,ID,GI)") Filiere filiere,
String langue) {
    @Override
    public String toString() {
        return (new ArrayList<>(cnes())).toString();
    }
}

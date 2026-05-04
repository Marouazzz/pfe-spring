package org.sid.pfespring.dto;

import java.util.ArrayList;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.sid.pfespring.model.Filiere;

public record RequestPFEDTO(Set<@NotBlank @Pattern(regexp="^[A-Z][1-9][0-9]{8}") String> cnes,
                            @NotBlank String sujet,
                            String description,
                            @NotNull Filiere filiere,
                            String langue) {
    public String toString() {
        return (new ArrayList<>(cnes())).toString();
    }
}

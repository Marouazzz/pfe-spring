package org.sid.pfespring.dto;

public record ResponseSalleDTO(
        Long id,
        String nomSalle,
        int capacite,
        boolean disponible
) {}
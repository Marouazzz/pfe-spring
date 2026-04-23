package org.sid.pfespring.dto;

import java.util.List;
import java.time.LocalDate;

public record RequestSoutenanceDTO(
        List<LocalDate> jours
) {}
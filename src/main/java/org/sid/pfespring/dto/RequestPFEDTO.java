package org.sid.pfespring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RequestPFEDTO (@NotBlank @Pattern(regexp="^[A-Z][1-9][0-9]{8}") String cne,@NotBlank String sujet,String description){}

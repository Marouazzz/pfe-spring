package org.sid.pfespring.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "salles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_salle", unique = true, nullable = false)
    private String nomSalle;

    @Column(nullable = false)
    private int capacite;

    @Column(nullable = false)
    private boolean disponible;
}
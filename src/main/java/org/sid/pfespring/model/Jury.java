package org.sid.pfespring.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jurys")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pfe_id", nullable = false, unique = true)
    private PFE pfe;

    // encadrant.getProf() — copié ici pour lecture directe
    @ManyToOne
    @JoinColumn(name = "encadrant_id", nullable = false)
    private Prof encadrant;

    // prof technique le moins chargé, ≠ encadrant
    @ManyToOne
    @JoinColumn(name = "prof1_id", nullable = false)
    private Prof prof1;

    // prof de langue si dispo, sinon prof technique (fallback)
    @ManyToOne
    @JoinColumn(name = "prof2_id", nullable = true)
    private Prof prof2;
}
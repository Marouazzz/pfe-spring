package org.sid.pfespring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    // encadrant.getProf() — copied ici pour lecture directe
    @ManyToOne
    @JoinColumn(name = "encadrant_id", nullable = false)
    private Prof encadrant;

    // prof technique le moins charge, ≠ encadrant
    @ManyToOne
    @JoinColumn(name = "prof1_id", nullable = false)
    private Prof prof1;

    // prof de langue si dispo, sinon prof technique (fallback)
    @ManyToOne
    @JoinColumn(name = "prof2_id", nullable = true)
    private Prof prof2;
    
    @ManyToOne
    @JoinColumn(name="version_id")
    private ImportVersion version;
}
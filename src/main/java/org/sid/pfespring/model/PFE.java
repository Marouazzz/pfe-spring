package org.sid.pfespring.model;

import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity(name="pfes")
@Getter
@Setter
@ToString(exclude = {"etudiants", "encadrant"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class PFE {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String sujet;

    @Column
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status = Status.ENCOURS;
    //langue ajoute
    @Enumerated(EnumType.STRING)
    @Column(name = "langue", nullable = true)
    private Langue langue;

    @OneToMany(mappedBy="pfe")
    private Set<Etudiant> etudiants;

    @ManyToOne
    @JoinColumn(name="encadrant_id")
    private Encadrant encadrant;

    @OneToOne(mappedBy = "pfe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Jury jury;
}

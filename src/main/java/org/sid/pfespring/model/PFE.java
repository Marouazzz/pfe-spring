package org.sid.pfespring.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
    private Status status = Status.DRAFT;
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

    @ManyToOne
    @JoinColumn(name="version_id")
    private ImportVersion version;
}

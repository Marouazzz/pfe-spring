package org.sid.pfespring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity(name="pfes")
@Data
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class PFE {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String sujet;

    @Column(nullable=false)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status = Status.ENCOURS;

    // Define domaine after NLP
    // ......................

    @OneToOne
    @JoinColumn(name="etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name="prof_id")
    private Prof prof;
}

package org.sid.pfespring.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "soutenances")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "pfe_id", nullable = false, unique = true)
    private PFE pfe;

    @OneToOne
    @JoinColumn(name = "jury_id", nullable = false, unique = true)
    private Jury jury;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(nullable = false)
    private LocalDate dateSoutenance;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;
}
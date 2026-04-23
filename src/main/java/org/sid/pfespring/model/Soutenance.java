package org.sid.pfespring.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "soutenances", uniqueConstraints = @UniqueConstraint(columnNames = {"salle_id", "date_soutenance", "heure_debut"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pfe_id", nullable = false)
    private PFE pfe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jury_id", nullable = false)
    private Jury jury;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(name = "date_soutenance", nullable = false)
    private LocalDate dateSoutenance;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;
}
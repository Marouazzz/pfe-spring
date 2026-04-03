package org.sid.pfespring.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etudiants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etudiant {

    @Id
    @Column(name = "cne", unique = true, nullable = false, length = 20)
    private String cne;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "filiere", nullable = false)
    private Filiere filiere;
}
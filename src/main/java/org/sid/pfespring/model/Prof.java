package org.sid.pfespring.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "profs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Prof {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nom" , nullable = false, length = 100)
    private String nom;
    @Column(name = "prenom" , nullable = false, length = 100)
    private String prenom;
    @Column(name = "maxEtudiants" , nullable = false, length = 100)

    private Integer maxEtudiants;
    @Enumerated(EnumType.STRING)
    @Column(name = "specialite" , nullable = false)

    private Specialite specialite ;
}

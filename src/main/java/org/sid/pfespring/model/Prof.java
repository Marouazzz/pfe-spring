package org.sid.pfespring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;

@Entity 
@Table(name = "profs")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
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

    @Override
    public String toString(){
        return nom + " " + prenom;
    }
}

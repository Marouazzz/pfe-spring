package org.sid.pfespring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "profs")
@Getter
@Setter
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

    private String specialite ;

    @ManyToOne
    @JoinColumn(name="version_id")
    private ImportVersion version;

    @Override
    public String toString(){
        return nom + " " + prenom;
    }
}

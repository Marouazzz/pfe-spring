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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name ="etudiants")
@Getter
@Setter
@ToString(exclude = "pfe")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etudiant {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id ;
    @Column(name = "cne",nullable = false,length = 10)
    private String cne;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "filiere", nullable = false)
    private Filiere filiere;

    @ManyToOne
    @JoinColumn(name="pfe_id")
    private PFE pfe;

    @ManyToOne
    @JoinColumn(name="version_id")
    private ImportVersion version;

    @Override
    public String toString(){
        return nom + " " + prenom;
    }

    @Override
public int hashCode() {
    return cne != null ? cne.hashCode() : 0;
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Etudiant)) return false;
    Etudiant e = (Etudiant) o;
    return cne != null && cne.equals(e.cne);
}
}
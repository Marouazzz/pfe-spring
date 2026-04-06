package org.sid.pfespring.model;

import jakarta.persistence.Entity;
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


    // private String sujet;
    // private String description;

    @OneToOne
    @JoinColumn(name="etudiant_id")
    private Etudiant etudiant;

    @ManyToOne
    @JoinColumn(name="prof_id")
    private Prof prof;
}

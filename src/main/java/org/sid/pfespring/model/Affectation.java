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




@Entity(name="affectations")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Affectation {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    // private String sujet;
    // private String description;
    
    
    @OneToOne
    @JoinColumn(name = "pfe_id") 
    private PFE pfe;

    @ManyToOne
    @JoinColumn(name = "prof_id")
    private Prof prof;
}

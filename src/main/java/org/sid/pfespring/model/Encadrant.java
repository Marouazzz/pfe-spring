package org.sid.pfespring.model;


import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encadrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Prof prof;

    @OneToMany(mappedBy = "encadrant")
    private List<PFE> pfes;

    @ManyToOne
    @JoinColumn(name="version_id")
    private ImportVersion version;

}

package org.sid.pfespring.services;


import java.util.Collections;
import java.util.List;

import org.sid.pfespring.model.Affectation;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.sid.pfespring.repository.AffectationRepository;
import org.sid.pfespring.repository.EtudiantRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class AffectationServiceImpl implements AffectationService {

    private AffectationRepository affectrepo;
    private PFERepository pferepo;
    private ProfRepository profrepo;
    private EtudiantRepository etudrepo;


    public AffectationServiceImpl(AffectationRepository repository,PFERepository pfeRepository,ProfRepository profRepository,EtudiantRepository etudiantRepository){
        this.affectrepo = repository;
        this.pferepo= pfeRepository;
        this.profrepo = profRepository;
        this.etudrepo = etudiantRepository;
    }

    @Override
    @Transactional
    public int affecterProfPFE() {
        // Recuperer les professeurs sauf ceux de langues 
        List<Specialite> langues  = List.of(Specialite.ANGLAIS,Specialite.FRANCAIS);
        List<Prof> profs = this.profrepo.findBySpecialiteNotIn(langues);
        List<Etudiant> etudiants = this.etudrepo.findAll();
        int affectationTotal = 0;


        // Compter prof par etudiant 
        if(etudiants != null && profs !=null && !etudiants.isEmpty() && !profs.isEmpty()){
            // Apply a shuffle 
            Collections.shuffle(profs);
            Collections.shuffle(etudiants);

            if(etudiants.size() < profs.size()){
                // realiser un chuffle etudiant par prof / ou bien declencher un erreur 
                // Will be done later after discussion 
            }else{
                // Calculer combien des etudiant affecter a un prof
                int capacite_minimum = etudiants.size() / profs.size();
                // Combien des etudiants restant
                int reste_etud = etudiants.size() % profs.size();
                int indexEtudiant = 0;
                for (Prof prof : profs){
                    int count = capacite_minimum + (reste_etud > 0 ? 1:0 );
                    for(int i=0; i < count ;i++){
                        PFE pfe = PFE.builder()
                        .etudiant(etudiants.get(indexEtudiant))
                        .prof(prof)
                        .build();
                        pfe = this.pferepo.save(pfe);
                        Affectation affectation = Affectation.builder()
                        .pfe(pfe)
                        .prof(prof)
                        .build();
                        affectation = this.affectrepo.save(affectation);
                        affectationTotal++;
                        indexEtudiant++;
                    }
                    reste_etud--;
                }
            }
        }else{
                // Throw errors based on unsatisfied condition
                // To be updated
                return 0;
            }
        return affectationTotal;
    }

    @Override
    public Object creer(Object request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Object> listerTous() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    

    
}

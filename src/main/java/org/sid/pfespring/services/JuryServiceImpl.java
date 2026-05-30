package org.sid.pfespring.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sid.pfespring.dto.RequestJuryDTO;
import org.sid.pfespring.dto.ResponseJuryDTO;
import org.sid.pfespring.exception.BusinessException;
import org.sid.pfespring.mapper.JuryMapper;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class JuryServiceImpl
        extends AbstractService<Jury, RequestJuryDTO, ResponseJuryDTO>
        implements JuryService {

    private final PFERepository pfeRepository;
    private final ProfRepository profRepository;
    private final ImportVersionRepository versrepository;
    private final FileSystemService fsService;
    private final SoutenanceRepository soutenanceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Rôles libres par jury (prof1 + prof2)
    private static final int ROLES_LIBRES_PAR_JURY = 2;

    // Spécialités considérées comme "langue" (String, plus enum)


    private final JuryRepository juryRepository;

    public JuryServiceImpl(JuryRepository juryRepository,
                           JuryMapper juryMapper,
                           PFERepository pfeRepository,
                           ProfRepository profRepository,
                           ImportVersionRepository versRepository,
                           FileSystemService fsService,
                           SoutenanceRepository soutenanceRepository) {
        super(juryRepository, juryMapper);
        this.juryRepository     = juryRepository;
        this.pfeRepository      = pfeRepository;
        this.profRepository     = profRepository;
        this.versrepository     = versRepository;
        this.fsService          = fsService;
        this.soutenanceRepository = soutenanceRepository;
    }


    @Override
    @Transactional
    public void affecterJury(Long id) {

        ImportVersion current_version = versrepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Lien invalide. Veuillez ré-importer le fichier Excel."));

        // CHECK 1 — PFEs existent ?
        List<PFE> tousLesPfes = pfeRepository.findByVersion(current_version);
        if (tousLesPfes.isEmpty())
            throw new BusinessException("Aucun PFE trouvé. Importez d'abord le fichier Excel.");

        // CHECK 2 — Encadrants affectés ?
        boolean encadrantsAffectes = tousLesPfes.stream()
                .anyMatch(pfe -> pfe.getEncadrant() != null);
        if (!encadrantsAffectes)
            throw new BusinessException(
                    "Encadrants non affectés. Cliquez d'abord sur 'Affecter les encadrants'.");

        // Suppression des anciens jurys (flush pour respecter les contraintes FK)
        soutenanceRepository.deleteSoutenancesByVersion(current_version);
        entityManager.flush();
        juryRepository.deleteByVersionJpql(current_version);
        entityManager.flush();
        entityManager.clear();

        // Recharger après clear
        List<PFE> pfes = pfeRepository.findByVersion(current_version).stream()
                .filter(pfe -> pfe.getEncadrant() != null)
                .collect(Collectors.toList());

        // Shuffle des PFEs pour varier les résultats
        Collections.shuffle(pfes);

        // Pool unique de tous les profs de cette version
        List<Prof> tousLesProfs = new ArrayList<>(profRepository.findByVersion(current_version));

        if (tousLesProfs.isEmpty())
            throw new BusinessException("Aucun professeur disponible dans ce fichier.");

        // Shuffle des profs
        Collections.shuffle(tousLesProfs);


        Map<Long, Integer> chargeParProf = new HashMap<>();
        tousLesProfs.forEach(p -> chargeParProf.put(p.getId(), 0));
        for (PFE pfe : pfes) {
            chargeParProf.merge(pfe.getEncadrant().getProf().getId(), 1, Integer::sum);
        }

        /*
         * Seuil max : répartition idéale des ROLES_LIBRES_PAR_JURY rôles/PFE
         * sur tous les profs. +1 de marge pour éviter tout blocage.
         */
        int seuilMax = (int) Math.ceil(
                (double) (pfes.size() * ROLES_LIBRES_PAR_JURY + 1) / tousLesProfs.size()
        ) + 1;

        List<Jury> jurysACreer = new ArrayList<>();

        for (PFE pfe : pfes) {

            Prof encadrantProf = pfe.getEncadrant().getProf();
            Long encadrantId   = encadrantProf.getId();

            Prof prof1;
            Prof prof2;

            if (isLangueProf(encadrantProf, pfe.getLangue())) {

                prof1 = choisirProf(tousLesProfs, encadrantId, null, chargeParProf, seuilMax);
                if (prof1 != null) chargeParProf.merge(prof1.getId(), 1, Integer::sum);

                prof2 = choisirProf(tousLesProfs, encadrantId,
                        prof1 != null ? prof1.getId() : null, chargeParProf, seuilMax);
                if (prof2 != null) chargeParProf.merge(prof2.getId(), 1, Integer::sum);

            } else {

                prof1 = choisirProfLangue(tousLesProfs, pfe.getLangue(),
                        encadrantId, chargeParProf, seuilMax);

                if (prof1 == null) {
                    // Aucun prof de langue dispo → prof normal moins chargé
                    prof1 = choisirProf(tousLesProfs, encadrantId, null, chargeParProf, seuilMax);
                }
                if (prof1 != null) chargeParProf.merge(prof1.getId(), 1, Integer::sum);


                prof2 = choisirProf(tousLesProfs, encadrantId,
                        prof1 != null ? prof1.getId() : null, chargeParProf, seuilMax);
                if (prof2 != null) chargeParProf.merge(prof2.getId(), 1, Integer::sum);
            }

            jurysACreer.add(Jury.builder()
                    .pfe(pfe)
                    .encadrant(encadrantProf)
                    .prof1(prof1)
                    .prof2(prof2)
                    .version(current_version)
                    .build());
        }

        repository.saveAll(jurysACreer);
    }
//private meth
    private boolean isLangueProf(Prof prof, String languePfe) {
        return prof.getSpecialite() != null
                && languePfe != null
                && prof.getSpecialite().equalsIgnoreCase(languePfe);
    }


    private Prof choisirProfLangue(List<Prof> profs,
                                   String languePfe,
                                   Long excludeEncadrantId,
                                   Map<Long, Integer> chargeParProf,
                                   int seuilMax) {
        if (languePfe == null || languePfe.isBlank()) return null;

        String langueUp = languePfe.toUpperCase();


        return profs.stream()
                .filter(p -> !p.getId().equals(excludeEncadrantId))
                .filter(p -> langueUp.equals(
                        p.getSpecialite() != null ? p.getSpecialite().toUpperCase() : ""))
                .filter(p -> chargeParProf.getOrDefault(p.getId(), 0) < seuilMax)
                .min(Comparator.comparingInt(p -> chargeParProf.getOrDefault(p.getId(), 0)))
                .orElse(null); // null = saturés → fallback dans affecterJury()
    }


    private Prof choisirProf(List<Prof> profs,
                             Long excludeId1,
                             Long excludeId2,
                             Map<Long, Integer> chargeParProf,
                             int seuilMax) {
        return profs.stream()
                .filter(p -> !p.getId().equals(excludeId1))
                .filter(p -> excludeId2 == null || !p.getId().equals(excludeId2))
                .filter(p -> chargeParProf.getOrDefault(p.getId(), 0) < seuilMax)
                .min(Comparator.comparingInt(p -> chargeParProf.getOrDefault(p.getId(), 0)))
                .orElseGet(() ->
                        profs.stream()
                                .filter(p -> !p.getId().equals(excludeId1))
                                .filter(p -> excludeId2 == null || !p.getId().equals(excludeId2))
                                .min(Comparator.comparingInt(
                                        p -> chargeParProf.getOrDefault(p.getId(), 0)))
                                .orElse(null)
                );
    }
    @Override
    public void genererPV(Long id) {
        ImportVersion version = versrepository.findById(id).get();
        List<Jury> jurys = ((JuryRepository) repository).findByVersion(version);
        jurys.forEach(fsService::generatePVFile);
    }
}
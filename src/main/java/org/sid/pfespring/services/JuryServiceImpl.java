package org.sid.pfespring.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sid.pfespring.dto.RequestJuryDTO;
import org.sid.pfespring.dto.ResponseJuryDTO;
import org.sid.pfespring.mapper.JuryMapper;
import org.sid.pfespring.model.*;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JuryServiceImpl
        extends AbstractService<Jury, RequestJuryDTO, ResponseJuryDTO>
        implements JuryService {

    private final PFERepository  pfeRepository;
    private final ProfRepository profRepository;

    private static final int ROLES_LIBRES_PAR_JURY = 2;

    public JuryServiceImpl(JuryRepository juryRepository,
                           JuryMapper juryMapper,
                           PFERepository pfeRepository,
                           ProfRepository profRepository) {
        super(juryRepository, juryMapper);
        this.pfeRepository  = pfeRepository;
        this.profRepository = profRepository;
    }

    @Override
    @Transactional
    public List<ResponseJuryDTO> affecterJury() {

        List<PFE> pfes = pfeRepository.findAll().stream()
                .filter(pfe -> pfe.getEncadrant() != null)
                .collect(Collectors.toList());

        if (pfes.isEmpty()) return Collections.emptyList();

        List<Specialite> specialitesLangue = List.of(Specialite.ANGLAIS, Specialite.FRANCAIS);
        List<Prof> profsTech     = profRepository.findBySpecialiteNotIn(specialitesLangue);
        List<Prof> profsAnglais  = profRepository.findBySpecialite(Specialite.ANGLAIS);
        List<Prof> profsFrancais = profRepository.findBySpecialite(Specialite.FRANCAIS);

        int totalProfs = profsTech.size() + profsAnglais.size() + profsFrancais.size();
        if (totalProfs == 0) return Collections.emptyList();

        int seuilMax = (int) Math.ceil(
                (double)(pfes.size() * ROLES_LIBRES_PAR_JURY) / totalProfs
        );

        Map<Long, Integer> chargeParProf = new HashMap<>();
        profsTech.forEach(p     -> chargeParProf.put(p.getId(), 0));
        profsAnglais.forEach(p  -> chargeParProf.put(p.getId(), 0));
        profsFrancais.forEach(p -> chargeParProf.put(p.getId(), 0));

        for (PFE pfe : pfes) {
            //  on extrait lid explicitement pour eviter les faux egaux entre

            Long encId = pfe.getEncadrant().getProf().getId();
            chargeParProf.merge(encId, 1, Integer::sum);
        }

        List<Jury> jurysACreer = new ArrayList<>();

        for (PFE pfe : pfes) {

            Prof encadrantProf = pfe.getEncadrant().getProf();
            // comparer uniquement par id (Long), pas par reference d'objet
            Long encadrantId = encadrantProf.getId();

            // PROF1 : tech le moins charge ≠ encadrant (comparaison par id)
            Prof prof1 = profsTech.stream()
                    .filter(p -> !p.getId().equals(encadrantId))          // FIX
                    .filter(p -> chargeParProf.getOrDefault(p.getId(), 0) < seuilMax)
                    .min(Comparator.comparingInt(
                            p -> chargeParProf.getOrDefault(p.getId(), 0)))
                    .orElseGet(() ->
                            profsTech.stream()
                                    .filter(p -> !p.getId().equals(encadrantId)) // FIX
                                    .min(Comparator.comparingInt(
                                            p -> chargeParProf.getOrDefault(p.getId(), 0)))
                                    .orElse(null)
                    );

            if (prof1 != null) chargeParProf.merge(prof1.getId(), 1, Integer::sum);

            Prof prof2 = null;

            if (pfe.getLangue() == Langue.FRANCAIS && !profsFrancais.isEmpty()) {

                prof2 = profsFrancais.stream()
                        .filter(p -> chargeParProf.getOrDefault(p.getId(), 0) < seuilMax)
                        .min(Comparator.comparingInt(
                                p -> chargeParProf.getOrDefault(p.getId(), 0)))
                        .orElse(null);

                if (prof2 != null) {
                    chargeParProf.merge(prof2.getId(), 1, Integer::sum);
                } else {
                    prof2 = fallbackTech(profsTech, encadrantId, prof1, chargeParProf, seuilMax);
                }

            } else if (pfe.getLangue() == Langue.ANGLAIS && !profsAnglais.isEmpty()) {

                prof2 = profsAnglais.stream()
                        .filter(p -> chargeParProf.getOrDefault(p.getId(), 0) < seuilMax)
                        .min(Comparator.comparingInt(
                                p -> chargeParProf.getOrDefault(p.getId(), 0)))
                        .orElse(null);

                if (prof2 != null) {
                    chargeParProf.merge(prof2.getId(), 1, Integer::sum);
                } else {
                    prof2 = fallbackTech(profsTech, encadrantId, prof1, chargeParProf, seuilMax);
                }

            } else {
                prof2 = fallbackTech(profsTech, encadrantId, prof1, chargeParProf, seuilMax);
            }

            jurysACreer.add(Jury.builder()
                    .pfe(pfe)
                    .encadrant(encadrantProf)
                    .prof1(prof1)
                    .prof2(prof2)
                    .build());
        }

        List<Jury> saved = repository.saveAll(jurysACreer);
        return mapper.toResponseList(saved);
    }

    private Prof fallbackTech(List<Prof> profsTech,
                              Long encadrantId,              // FIX : Long, pas Prof
                              Prof prof1,
                              Map<Long, Integer> chargeParProf,
                              int seuilMax) {
        Prof fallback = profsTech.stream()
                .filter(p -> !p.getId().equals(encadrantId))               // FIX
                .filter(p -> prof1 == null || !p.getId().equals(prof1.getId()))
                .filter(p -> chargeParProf.getOrDefault(p.getId(), 0) < seuilMax)
                .min(Comparator.comparingInt(
                        p -> chargeParProf.getOrDefault(p.getId(), 0)))
                .orElseGet(() ->
                        profsTech.stream()
                                .filter(p -> !p.getId().equals(encadrantId)) // FIX
                                .filter(p -> prof1 == null || !p.getId().equals(prof1.getId()))
                                .min(Comparator.comparingInt(
                                        p -> chargeParProf.getOrDefault(p.getId(), 0)))
                                .orElse(null)
                );

        if (fallback != null) chargeParProf.merge(fallback.getId(), 1, Integer::sum);
        return fallback;
    }

    @Override
    public byte[] exportJuryExcel() throws IOException {
        List<Jury> jurys = repository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Jurys PFE");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);
            String[] cols = {"N°", "Sujet PFE", "CNEs",
                    "Encadrant", "Membre Technique", "Membre Langue/Fallback"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Jury jury : jurys) {
                Row row = sheet.createRow(rowNum);

                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(jury.getPfe().getSujet());

                String cnes = jury.getPfe().getEtudiants().stream()
                        .map(Etudiant::getCne)
                        .collect(Collectors.joining(", "));
                row.createCell(2).setCellValue(cnes);

                row.createCell(3).setCellValue(
                        jury.getEncadrant().getNom() + " " + jury.getEncadrant().getPrenom());

                row.createCell(4).setCellValue(jury.getProf1() != null
                        ? jury.getProf1().getNom() + " " + jury.getProf1().getPrenom() : "N/A");

                row.createCell(5).setCellValue(jury.getProf2() != null
                        ? jury.getProf2().getNom() + " " + jury.getProf2().getPrenom()
                        + " (" + jury.getProf2().getSpecialite() + ")" : "N/A");

                rowNum++;
            }

            for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
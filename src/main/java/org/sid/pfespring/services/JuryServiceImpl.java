package org.sid.pfespring.services;
import org.sid.pfespring.exception.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sid.pfespring.dto.RequestJuryDTO;
import org.sid.pfespring.dto.ResponseJuryDTO;
import org.sid.pfespring.mapper.JuryMapper;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.Langue;
import org.sid.pfespring.model.PFE;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.PFERepository;
import org.sid.pfespring.repository.ProfRepository;
import org.sid.pfespring.utils.ExcelTheme;
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

    //  appeler entityManager.flush() et executer delete avant insert( prob dunicite jury-pfe)
    @PersistenceContext
    private EntityManager entityManager;

    private static final int ROLES_LIBRES_PAR_JURY = 2;
    private final JuryRepository juryRepository;
    public JuryServiceImpl(JuryRepository juryRepository,
                           JuryMapper juryMapper,
                           PFERepository pfeRepository,
                           ProfRepository profRepository,
                           ImportVersionRepository versRepository) {
        super(juryRepository, juryMapper);
        this.juryRepository = juryRepository;
        this.pfeRepository = pfeRepository;
        this.profRepository = profRepository;
        this.versrepository = versRepository;
    }


    @Override
    @Transactional
    public List<ResponseJuryDTO> affecterJury(Long id) {
        ImportVersion current_version = versrepository.findById(id).orElseThrow(() -> new BusinessException("Lien invalide. Veuillez ré-importer le fichier Excel."));
        // CHECK 1 — PFEs existent pour cette version ?
        List<PFE> tousLesPfes = pfeRepository.findByVersion(current_version);
        if (tousLesPfes.isEmpty())
            throw new BusinessException("Aucun PFE trouvé. Importez d'abord le fichier Excel.");

        // CHECK 2 — Encadrants affectés ?
        boolean encadrantsAffectes = tousLesPfes.stream()
                .anyMatch(pfe -> pfe.getEncadrant() != null);
        if (!encadrantsAffectes)
            throw new BusinessException("Encadrants non affectés. Cliquez d'abord sur 'Affecter les encadrants'.");

        // Supprimer les anciens jurys de cette version avant de recréer
        juryRepository.deleteByVersionJpql(current_version);
        entityManager.flush();
        entityManager.clear();
        List<PFE> pfes = pfeRepository.findByVersion(current_version).stream()
                .filter(pfe -> pfe.getEncadrant() != null)
                .collect(Collectors.toList());

        //  AJOUT 2 : shuffler les PFEs pour diff res
        Collections.shuffle(pfes);
        List<Specialite> specialitesLangue = List.of(Specialite.ANGLAIS, Specialite.FRANCAIS);
        // AJOUT 3
        List<Prof> profsTech = new ArrayList<>(profRepository.findByVersionAndSpecialiteNotIn(current_version, specialitesLangue));
        List<Prof> profsAnglais = new ArrayList<>(profRepository.findByVersionAndSpecialite(current_version, Specialite.ANGLAIS));
        List<Prof> profsFrancais = new ArrayList<>(profRepository.findByVersionAndSpecialite(current_version, Specialite.FRANCAIS));
        //  AJOUT 4 : shuffler les profs pour
        Collections.shuffle(profsTech);
        Collections.shuffle(profsAnglais);
        Collections.shuffle(profsFrancais);

        int totalProfs = profsTech.size() + profsAnglais.size() + profsFrancais.size();
        //if (totalProfs == 0) return Collections.emptyList();
        if (totalProfs == 0)
            throw new BusinessException("Aucun professeur disponible dans ce fichier.");

        int seuilMax = (int) Math.ceil(
                (double) (pfes.size() * ROLES_LIBRES_PAR_JURY) / totalProfs
        );

        Map<Long, Integer> chargeParProf = new HashMap<>();
        profsTech.forEach(p -> chargeParProf.put(p.getId(), 0));
        profsAnglais.forEach(p -> chargeParProf.put(p.getId(), 0));
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
                    .version(current_version)
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
    @Transactional
    @Override
    public byte[] exportJuryExcel(Long id) throws IOException {
        ImportVersion current_version = versrepository.findById(id).get();
        List<Jury> jurys = ((JuryRepository) repository).findByVersion(current_version);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create styles first
            Map<String, CellStyle> styles = createStyles(workbook);

            Sheet sheet = workbook.createSheet("Planning des Jurys PFE");

            // Set column widths (PLUS LARGES pour voir tout le contenu)
            sheet.setColumnWidth(0, 4000);  // N°
            sheet.setColumnWidth(1, 15000); // Sujet PFE (TRÈS LARGE pour les longs sujets)
            sheet.setColumnWidth(2, 8000);  // CNEs (pour plusieurs CNEs)
            sheet.setColumnWidth(3, 9000);  // Encadrant (Nom + Prénom)
            sheet.setColumnWidth(4, 9000);  // Prof1
            sheet.setColumnWidth(5, 9000);  // Prof2

            // Create HEADER row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);

            String[] headers = {"N°", "Sujet PFE", "CNEs", "Encadrant", "Professeur 1", "Professeur 2"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(styles.get("header"));
            }

            // Fill data rows
            int rowNum = 1;
            for (Jury jury : jurys) {
                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(20);

                // Column 0: N° (CENTRÉ)
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(rowNum);
                cell0.setCellStyle(styles.get(rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center"));

                // Column 1: Sujet PFE (CENTRÉ maintenant)
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(jury.getPfe().getSujet());
                cell1.setCellStyle(styles.get(rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center"));

                // Column 2: CNEs (CENTRÉ)
                Cell cell2 = row.createCell(2);
                String cnes = jury.getPfe().getEtudiants().stream()
                        .map(Etudiant::getCne)
                        .collect(Collectors.joining(", "));
                cell2.setCellValue(cnes);
                cell2.setCellStyle(styles.get(rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center"));

                // Column 3: Encadrant (CENTRÉ)
                Cell cell3 = row.createCell(3);
                String encadrant = jury.getEncadrant().getNom() + " " + jury.getEncadrant().getPrenom();
                cell3.setCellValue(encadrant);
                // Use palette colors for encadrant based on name hash for consistency
                String encadrantColorKey = "prof_" + (Math.abs(encadrant.hashCode()) % ExcelTheme.PROF_PALETTE.length);
                if (!styles.containsKey(encadrantColorKey)) {
                    styles.put(encadrantColorKey, createProfStyle(workbook, encadrantColorKey,
                            ExcelTheme.PROF_PALETTE[Math.abs(encadrant.hashCode()) % ExcelTheme.PROF_PALETTE.length], true));
                }
                cell3.setCellStyle(styles.get(encadrantColorKey));

                // Column 4: Professeur 1 (CENTRÉ)
                Cell cell4 = row.createCell(4);
                String prof1 = jury.getProf1() != null
                        ? jury.getProf1().getNom() + " " + jury.getProf1().getPrenom()
                        : "Non assigné";
                cell4.setCellValue(prof1);
                String prof1Key = "prof_" + prof1.hashCode();
                if (!styles.containsKey(prof1Key) && jury.getProf1() != null) {
                    styles.put(prof1Key, createProfStyle(workbook, prof1Key,
                            ExcelTheme.PROF_PALETTE[Math.abs(prof1.hashCode()) % ExcelTheme.PROF_PALETTE.length], true));
                }
                cell4.setCellStyle(styles.getOrDefault(prof1Key, styles.get(rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center")));

                // Column 5: Professeur 2 (CENTRÉ)
                Cell cell5 = row.createCell(5);
                String prof2 = jury.getProf2() != null
                        ? jury.getProf2().getNom() + " " + jury.getProf2().getPrenom()
                        : "Non assigné";
                cell5.setCellValue(prof2);
                String prof2Key = "prof_" + prof2.hashCode();
                if (!styles.containsKey(prof2Key) && jury.getProf2() != null) {
                    styles.put(prof2Key, createProfStyle(workbook, prof2Key,
                            ExcelTheme.PROF_PALETTE[Math.abs(prof2.hashCode()) % ExcelTheme.PROF_PALETTE.length], true));
                }
                cell5.setCellStyle(styles.getOrDefault(prof2Key, styles.get(rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center")));

                rowNum++;
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        byte[] headerBg = ExcelTheme.hexToBytes(ExcelTheme.HEADER_BG);
        XSSFColor headerColor = new XSSFColor(headerBg, null);
        headerStyle.setFillForegroundColor(headerColor);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);

        // Odd row styles (white background) - TOUT CENTRÉ
        CellStyle oddCenter = workbook.createCellStyle();
        oddCenter.setBorderBottom(BorderStyle.THIN);
        oddCenter.setBorderLeft(BorderStyle.THIN);
        oddCenter.setBorderRight(BorderStyle.THIN);
        oddCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        oddCenter.setAlignment(HorizontalAlignment.CENTER);  // CENTRÉ
        byte[] oddBg = ExcelTheme.hexToBytes(ExcelTheme.ROW_IMPAIR);
        XSSFColor oddColor = new XSSFColor(oddBg, null);
        oddCenter.setFillForegroundColor(oddColor);
        oddCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_odd_center", oddCenter);

        // Even row styles (light gray background) - TOUT CENTRÉ
        CellStyle evenCenter = workbook.createCellStyle();
        evenCenter.setBorderBottom(BorderStyle.THIN);
        evenCenter.setBorderLeft(BorderStyle.THIN);
        evenCenter.setBorderRight(BorderStyle.THIN);
        evenCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        evenCenter.setAlignment(HorizontalAlignment.CENTER);  // CENTRÉ
        byte[] evenBg = ExcelTheme.hexToBytes(ExcelTheme.ROW_PAIR);
        XSSFColor evenColor = new XSSFColor(evenBg, null);
        evenCenter.setFillForegroundColor(evenColor);
        evenCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_even_center", evenCenter);

        return styles;
    }

    private CellStyle createProfStyle(XSSFWorkbook workbook, String key, String hexColor, boolean centered) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Centrer le texte si demandé
        if (centered) {
            style.setAlignment(HorizontalAlignment.CENTER);
        } else {
            style.setAlignment(HorizontalAlignment.LEFT);
        }

        byte[] bgBytes = ExcelTheme.hexToBytes(hexColor);
        XSSFColor bgColor = new XSSFColor(bgBytes, null);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }}
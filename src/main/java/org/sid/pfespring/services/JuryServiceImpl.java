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



//     @Override
//     public byte[] exportJuryExcel(Long id) throws IOException {
//         ImportVersion current_version = versrepository.findById(id).get();
//         List<Jury> jurys = ((JuryRepository) repository).findByVersion(current_version);

//         try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//             Map<String, CellStyle> styles = createStyles(workbook);
//             Sheet sheet = workbook.createSheet("Planning des Jurys PFE");

//             sheet.setColumnWidth(0, 4000);
//             sheet.setColumnWidth(1, 15000);
//             sheet.setColumnWidth(2, 8000);
//             sheet.setColumnWidth(3, 9000);
//             sheet.setColumnWidth(4, 9000);
//             sheet.setColumnWidth(5, 9000);

//             Row headerRow = sheet.createRow(0);
//             headerRow.setHeightInPoints(25);
//             String[] headers = {"N°", "Sujet PFE", "CNEs", "Encadrant", "Professeur 1", "Professeur 2"};
//             for (int i = 0; i < headers.length; i++) {
//                 Cell cell = headerRow.createCell(i);
//                 cell.setCellValue(headers[i]);
//                 cell.setCellStyle(styles.get("header"));
//             }

//             int rowNum = 1;
//             for (Jury jury : jurys) {
//                 Row row = sheet.createRow(rowNum);
//                 row.setHeightInPoints(20);
//                 String parity = rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center";

//                 Cell cell0 = row.createCell(0);
//                 cell0.setCellValue(rowNum);
//                 cell0.setCellStyle(styles.get(parity));

//                 Cell cell1 = row.createCell(1);
//                 cell1.setCellValue(jury.getPfe().getSujet());
//                 cell1.setCellStyle(styles.get(parity));

//                 Cell cell2 = row.createCell(2);
//                 String cnes = jury.getPfe().getEtudiants().stream()
//                         .map(Etudiant::getCne)
//                         .collect(Collectors.joining(", "));
//                 cell2.setCellValue(cnes);
//                 cell2.setCellStyle(styles.get(parity));

//                 Cell cell3 = row.createCell(3);
//                 String encadrant = jury.getEncadrant().getNom() + " " + jury.getEncadrant().getPrenom();
//                 cell3.setCellValue(encadrant);
//                 cell3.setCellStyle(getOrCreateProfStyle(styles, workbook, encadrant));

//                 Cell cell4 = row.createCell(4);
//                 String prof1 = jury.getProf1() != null
//                         ? jury.getProf1().getNom() + " " + jury.getProf1().getPrenom()
//                         : "Non assigné";
//                 cell4.setCellValue(prof1);
//                 cell4.setCellStyle(jury.getProf1() != null
//                         ? getOrCreateProfStyle(styles, workbook, prof1)
//                         : styles.get(parity));

//                 Cell cell5 = row.createCell(5);
//                 String prof2 = jury.getProf2() != null
//                         ? jury.getProf2().getNom() + " " + jury.getProf2().getPrenom()
//                         : "Non assigné";
//                 cell5.setCellValue(prof2);
//                 cell5.setCellStyle(jury.getProf2() != null
//                         ? getOrCreateProfStyle(styles, workbook, prof2)
//                         : styles.get(parity));

//                 rowNum++;
//             }

//             sheet.createFreezePane(0, 1);
//             ByteArrayOutputStream out = new ByteArrayOutputStream();
//             workbook.write(out);
//             return out.toByteArray();
//         }
//     }

//     private CellStyle getOrCreateProfStyle(Map<String, CellStyle> styles,
//                                            XSSFWorkbook workbook,
//                                            String profName) {
//         String key = "prof_" + Math.abs(profName.hashCode());
//         if (!styles.containsKey(key)) {
//             String hex = ExcelTheme.PROF_PALETTE[
//                     Math.abs(profName.hashCode()) % ExcelTheme.PROF_PALETTE.length];
//             styles.put(key, createProfStyle(workbook, hex));
//         }
//         return styles.get(key);
//     }

//     private Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
//         Map<String, CellStyle> styles = new HashMap<>();

//         CellStyle headerStyle = workbook.createCellStyle();
//         headerStyle.setFillForegroundColor(
//                 new XSSFColor(ExcelTheme.hexToBytes(ExcelTheme.HEADER_BG), null));
//         headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//         Font headerFont = workbook.createFont();
//         headerFont.setBold(true);
//         headerFont.setColor(IndexedColors.WHITE.getIndex());
//         headerFont.setFontHeightInPoints((short) 11);
//         headerStyle.setFont(headerFont);
//         headerStyle.setAlignment(HorizontalAlignment.CENTER);
//         headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//         headerStyle.setBorderBottom(BorderStyle.THIN);
//         headerStyle.setBorderTop(BorderStyle.THIN);
//         headerStyle.setBorderLeft(BorderStyle.THIN);
//         headerStyle.setBorderRight(BorderStyle.THIN);
//         styles.put("header", headerStyle);

//         CellStyle oddCenter = workbook.createCellStyle();
//         oddCenter.setBorderBottom(BorderStyle.THIN);
//         oddCenter.setBorderLeft(BorderStyle.THIN);
//         oddCenter.setBorderRight(BorderStyle.THIN);
//         oddCenter.setVerticalAlignment(VerticalAlignment.CENTER);
//         oddCenter.setAlignment(HorizontalAlignment.CENTER);
//         oddCenter.setFillForegroundColor(
//                 new XSSFColor(ExcelTheme.hexToBytes(ExcelTheme.ROW_IMPAIR), null));
//         oddCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//         styles.put("cell_odd_center", oddCenter);

//         CellStyle evenCenter = workbook.createCellStyle();
//         evenCenter.setBorderBottom(BorderStyle.THIN);
//         evenCenter.setBorderLeft(BorderStyle.THIN);
//         evenCenter.setBorderRight(BorderStyle.THIN);
//         evenCenter.setVerticalAlignment(VerticalAlignment.CENTER);
//         evenCenter.setAlignment(HorizontalAlignment.CENTER);
//         evenCenter.setFillForegroundColor(
//                 new XSSFColor(ExcelTheme.hexToBytes(ExcelTheme.ROW_PAIR), null));
//         evenCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//         styles.put("cell_even_center", evenCenter);

//         return styles;
//     }

//     private CellStyle createProfStyle(XSSFWorkbook workbook, String hexColor) {
//         CellStyle style = workbook.createCellStyle();
//         style.setBorderBottom(BorderStyle.THIN);
//         style.setBorderLeft(BorderStyle.THIN);
//         style.setBorderRight(BorderStyle.THIN);
//         style.setVerticalAlignment(VerticalAlignment.CENTER);
//         style.setAlignment(HorizontalAlignment.CENTER);
//         style.setFillForegroundColor(
//                 new XSSFColor(ExcelTheme.hexToBytes(hexColor), null));
//         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//         return style;
//     }


//     @Override
//     public byte[] exportJuryPDF(Long id) throws IOException {
//         ImportVersion current_version = versrepository.findById(id).get();
//         List<Jury> jurys = ((JuryRepository) repository).findByVersion(current_version);
//         return PDFGenerator.exportJuryPDF(jurys);
//     }


    @Override
    public void genererPV(Long id) {
        ImportVersion version = versrepository.findById(id).get();
        List<Jury> jurys = ((JuryRepository) repository).findByVersion(version);
        jurys.forEach(fsService::generatePVFile);
    }
}
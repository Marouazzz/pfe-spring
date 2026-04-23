package org.sid.pfespring.services;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;
import org.sid.pfespring.mapper.SalleMapper;
import org.sid.pfespring.mapper.SoutenanceMapper;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.Salle;
import org.sid.pfespring.model.Soutenance;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.SalleRepository;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Service

public class SalleServiceImpl
        extends AbstractService<Salle, RequestSalleDTO, ResponseSalleDTO>
        implements SalleService {


    private final SalleRepository      salleRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final JuryRepository       juryRepository;
    private final SalleMapper          salleMapper;
    private final SoutenanceMapper     soutenanceMapper;
    // Constructeur manuel obligatoire pour l'héritage
    public SalleServiceImpl(SalleRepository salleRepository,
                            SoutenanceRepository soutenanceRepository,
                            JuryRepository juryRepository,
                            SalleMapper salleMapper,
                            SoutenanceMapper soutenanceMapper) {
        // On passe les éléments spécifiques au AbstractService via super()
        super(salleRepository, salleMapper);
        this.salleRepository = salleRepository;
        this.soutenanceRepository = soutenanceRepository;
        this.juryRepository = juryRepository;
        this.salleMapper = salleMapper;
        this.soutenanceMapper = soutenanceMapper;
    }
    //Constantes horaires
    private static final LocalTime HEURE_DEBUT          = LocalTime.of(8, 30);
    private static final LocalTime HEURE_DERNIERE_DEBUT = LocalTime.of(17, 10);
    private static final int       DUREE_SOUTENANCE     = 60;   // minutes
    private static final int       PAUSE                = 5;    // minutes entre 2 soutenances
    private static final int       CRENEAU              = DUREE_SOUTENANCE + PAUSE; // 65 min


    //  IMPORT EXCEL sheet salles

    @Override
    public List<ResponseSalleDTO> importFromExcel(InputStream is) throws Exception {
        Workbook wb    = new XSSFWorkbook(is);
        Sheet    sheet = wb.getSheet("salles");

        if (sheet == null)
            throw new IllegalArgumentException("Sheet 'salles' introuvable dans le fichier Excel.");

        List<Salle> salles = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) continue;

            String nomSalle = row.getCell(0).getStringCellValue().trim();
            int    capacite = (int) row.getCell(1).getNumericCellValue();

            // Évite  doublons si on réimporte
            if (salleRepository.existsByNomSalle(nomSalle)) continue;

            salles.add(Salle.builder()
                    .nomSalle(nomSalle)
                    .capacite(capacite)
                    .disponible(true)   // ← toutes disponibles à l'import
                    .build());
        }
        wb.close();
        return salleRepository.saveAll(salles)
                .stream().map(salleMapper::toResponse).toList();
    }


    //  IMPORT EXCEL sheet jours_soutenances

    @Override
    public List<LocalDate> importJoursSoutenances(InputStream is) throws Exception {
        Workbook wb    = new XSSFWorkbook(is);
        Sheet    sheet = wb.getSheet("jours_soutenances");

        if (sheet == null)
            throw new IllegalArgumentException("Sheet 'jours_soutenances' introuvable.");

        List<LocalDate> jours = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) continue;

            Cell cell = row.getCell(0);
            if (cell == null) continue;

            // Skip empty cells
            if (cell.getCellType() == CellType.BLANK) continue;
            LocalDate date;


            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                date = cell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                date = LocalDate.parse(
                        cell.getStringCellValue().trim(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                );
            }
            jours.add(date);
        }
        wb.close();
        return jours;
    }

    // ══════════════════════════════════════════════════════════════
    //  ALGORITHME PRINCIPAL D'AFFECTATION
    // ══════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public List<ResponseSoutenanceDTO> affecterSalles(List<LocalDate> jours) {

        List<Jury>  jurys        = juryRepository.findAll();
        List<Salle> sallesDispos = salleRepository.findByDisponibleTrue();

        if (sallesDispos.isEmpty())
            throw new IllegalStateException(
                    "Aucune salle disponible. Importez d'abord les salles.");
        if (jurys.isEmpty())
            throw new IllegalStateException(
                    "Aucun jury trouvé. Lancez l'affectation des jurys avant.");

        // Génère tous les créneaux (date + heure) dans l'ordre chronologique
        List<Creneau> tousCreneaux = genererCreneaux(jours);

        // Seuil de soutenances par prof par jour pour équilibrer la charge
        // 75 soutenances × 3 rôles / 27 profs / 9 jours ≈ 1 par prof par jour
        int totalSout      = jurys.size();
        int nbJours        = jours.size();
        int seuilParProfParJour = Math.max(2,
                (int) Math.ceil((double)(totalSout * 3) / 27 / nbJours) + 1);

        List<Soutenance> resultat = new ArrayList<>();

        for (Jury jury : jurys) {
            Soutenance s = placerJury(jury, tousCreneaux, sallesDispos, seuilParProfParJour);
            if (s == null)
                throw new IllegalStateException(
                        "Impossible d'affecter le jury id=" + jury.getId()
                                + " (PFE: " + jury.getPfe().getSujet() + "). "
                                + "Contraintes non satisfiables avec les données actuelles.");

            soutenanceRepository.save(s);
            resultat.add(s);
        }

        // ── Marquer les salles comme non disponibles ───────────────
        // Après la 1ère affectation complète, les salles ne sont
        // plus "libres" pour une nouvelle session de planification
        sallesDispos.forEach(salle -> salle.setDisponible(false));
        salleRepository.saveAll(sallesDispos);

        return resultat.stream()
                .map(soutenanceMapper::toResponse)
                .toList();
    }

    // ─── Génère la liste ordonnée de tous les créneaux possibles ──
    // Ordre : jour par jour, créneau par créneau
    // 08:30, 09:35, 10:40, 11:45, 12:50, 13:55, 15:00, 16:05, 17:10
    private List<Creneau> genererCreneaux(List<LocalDate> jours) {
        List<Creneau> creneaux = new ArrayList<>();
        for (LocalDate jour : jours) {
            LocalTime heure = HEURE_DEBUT;
            while (!heure.isAfter(HEURE_DERNIERE_DEBUT)) {
                creneaux.add(new Creneau(jour, heure));
                int totalMin = heure.getHour() * 60 + heure.getMinute() + CRENEAU;
                heure = LocalTime.of(totalMin / 60, totalMin % 60);
            }
        }
        return creneaux;
    }

    // ─── Cherche le 1er créneau valide pour ce jury ───────────────
    private Soutenance placerJury(Jury jury,
                                  List<Creneau> creneaux,
                                  List<Salle> salles,
                                  int seuilParProfParJour) {

        Long encId = jury.getEncadrant().getId();
        Long p1Id  = jury.getProf1().getId();
        Long p2Id  = jury.getProf2().getId();

        for (Creneau c : creneaux) {
            LocalDate date  = c.date();
            LocalTime debut = c.heure();
            LocalTime fin   = debut.plusMinutes(DUREE_SOUTENANCE);

            // Contrainte A : aucun prof du jury déjà occupé sur ce créneau
            if (soutenanceRepository.existsProfConflict(date, debut, fin, encId)) continue;
            if (soutenanceRepository.existsProfConflict(date, debut, fin, p1Id))  continue;
            if (soutenanceRepository.existsProfConflict(date, debut, fin, p2Id))  continue;

            // Contrainte B : répartition équilibrée par jour
            if (soutenanceRepository.countByProfIdAndDate(encId, date) >= seuilParProfParJour) continue;
            if (soutenanceRepository.countByProfIdAndDate(p1Id,  date) >= seuilParProfParJour) continue;
            if (soutenanceRepository.countByProfIdAndDate(p2Id,  date) >= seuilParProfParJour) continue;

            // Contrainte C : une salle libre à ce créneau
            Salle salle = salles.stream()
                    .filter(sl -> !soutenanceRepository
                            .existsBySalleAndDateSoutenanceAndHeureDebutLessThanAndHeureFinGreaterThan(
                                    sl, date, fin, debut))
                    .findFirst()
                    .orElse(null);

            if (salle == null) continue;

            // ✅ Créneau valide trouvé
            return Soutenance.builder()
                    .pfe(jury.getPfe())
                    .jury(jury)
                    .salle(salle)
                    .dateSoutenance(date)
                    .heureDebut(debut)
                    .heureFin(fin)
                    .build();
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    //  EXPORT EXCEL — Planning complet
    // ══════════════════════════════════════════════════════════════
    @Override
    public byte[] exportPlanningExcel() throws Exception {

        List<Soutenance> soutenances =
                soutenanceRepository
                        .findAllByOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc();

        XSSFWorkbook wb    = new XSSFWorkbook();
        XSSFSheet    sheet = wb.createSheet("Planning Soutenances");

        // ── Styles ─────────────────────────────────────────────────
        XSSFCellStyle headerStyle = buildHeaderStyle(wb);
        XSSFCellStyle pairStyle   = buildRowStyle(wb, new byte[]{(byte)217,(byte)225,(byte)242});
        XSSFCellStyle impairStyle = buildRowStyle(wb, new byte[]{(byte)255,(byte)255,(byte)255});

        // ── En-têtes ───────────────────────────────────────────────
        String[] entetes = {
                "Sujet PFE", "Étudiant(s)", "Encadrant",
                "Prof 1 (Jury)", "Prof 2 (Jury)",
                "Date", "Heure début", "Heure fin", "Salle"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < entetes.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(entetes[i]);
            c.setCellStyle(headerStyle);
        }

        // ── Données ────────────────────────────────────────────────
        int rowNum = 1;
        for (Soutenance s : soutenances) {
            Row         row   = sheet.createRow(rowNum);
            XSSFCellStyle sty = (rowNum % 2 == 0) ? pairStyle : impairStyle;
            Jury        jury  = s.getJury();

            String etudiants = s.getPfe().getEtudiants().stream()
                    .map(e -> e.getNom() + " " + e.getPrenom())
                    .collect(Collectors.joining("\n"));

            ecrireCell(row, 0, s.getPfe().getSujet(), sty);
            ecrireCell(row, 1, etudiants, sty);
            ecrireCell(row, 2,
                    jury.getEncadrant().getNom() + " "
                            + jury.getEncadrant().getPrenom(), sty);
            ecrireCell(row, 3,
                    jury.getProf1().getNom() + " " + jury.getProf1().getPrenom(), sty);
            ecrireCell(row, 4,
                    jury.getProf2().getNom() + " " + jury.getProf2().getPrenom(), sty);
            ecrireCell(row, 5, s.getDateSoutenance().toString(), sty);
            ecrireCell(row, 6, s.getHeureDebut().toString(), sty);
            ecrireCell(row, 7, s.getHeureFin().toString(), sty);
            ecrireCell(row, 8, s.getSalle().getNomSalle(), sty);

            // Retour à la ligne dans la cellule étudiants si multiple
            if (etudiants.contains("\n")) {
                row.setHeight((short)(row.getHeight() * 2));
                XSSFCellStyle wrapStyle = wb.createCellStyle();
                wrapStyle.cloneStyleFrom(sty);
                wrapStyle.setWrapText(true);
                row.getCell(1).setCellStyle(wrapStyle);
            }
            rowNum++;
        }

        // ── Largeurs de colonnes ───────────────────────────────────
        int[] widths = {45, 38, 25, 25, 25, 14, 14, 14, 18};
        for (int i = 0; i < widths.length; i++)
            sheet.setColumnWidth(i, widths[i] * 256);

        // ── Figer la ligne d'en-tête ───────────────────────────────
        sheet.createFreezePane(0, 1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ─── Helpers styles Excel ─────────────────────────────────────
    private XSSFCellStyle buildHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Arial");
        style.setFont(font);
        style.setFillForegroundColor(
                new XSSFColor(new byte[]{(byte)31,(byte)78,(byte)121}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private XSSFCellStyle buildRowStyle(XSSFWorkbook wb, byte[] rgb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setFontName("Arial");
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(rgb, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private void setBorders(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void ecrireCell(Row row, int col, String val, XSSFCellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    // ─── Record interne pour un créneau ───────────────────────────
    private record Creneau(LocalDate date, LocalTime heure) {}
}
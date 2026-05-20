package org.sid.pfespring.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.dto.ResponseSoutenanceDTO;
import org.sid.pfespring.mapper.SalleMapper;
import org.sid.pfespring.mapper.SoutenanceMapper;
import org.sid.pfespring.model.*;
import org.sid.pfespring.repository.*;
import org.sid.pfespring.utils.ExcelTheme;
import org.sid.pfespring.utils.PDFGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalleServiceImpl
        extends AbstractService<Salle, RequestSalleDTO, ResponseSalleDTO>
        implements SalleService {


    //  CONSTANTES MÉTIER

    private static final LocalTime MATIN_DEBUT  = LocalTime.of(9,  0);
    private static final LocalTime MATIN_FIN    = LocalTime.of(12, 0);
    private static final LocalTime APMIDI_DEBUT = LocalTime.of(14, 0);
    private static final LocalTime APMIDI_FIN   = LocalTime.of(18, 0);
    private static final int DUREE_MIN       = 60;
    private static final int REPOS_MIN       = 60;
    private static final int MAX_JOURS_CAL   = 120;


    private final SalleRepository      salleRepository;
    private final SoutenanceRepository soutenanceRepository;
    private final JuryRepository       juryRepository;
    private final ImportVersionRepository versionRepository;
    private final SalleMapper          salleMapper;
    private final SoutenanceMapper     soutenanceMapper;

    public SalleServiceImpl(SalleRepository salleRepository,
                            SoutenanceRepository soutenanceRepository,
                            JuryRepository juryRepository,
                            ImportVersionRepository versionRepository,
                            SalleMapper salleMapper,
                            SoutenanceMapper soutenanceMapper) {
        super(salleRepository, salleMapper);
        this.salleRepository      = salleRepository;
        this.soutenanceRepository = soutenanceRepository;
        this.juryRepository       = juryRepository;
        this.versionRepository = versionRepository;
        this.salleMapper          = salleMapper;
        this.soutenanceMapper     = soutenanceMapper;
    }

    //  GÉNÉRATION DES CRÉNEAUX
    private List<LocalTime> genererCreneauxJour() {
        List<LocalTime> creneaux = new ArrayList<>();
        for (LocalTime t = MATIN_DEBUT; !t.plusMinutes(DUREE_MIN).isAfter(MATIN_FIN);
             t = t.plusMinutes(DUREE_MIN + REPOS_MIN)) creneaux.add(t);
        for (LocalTime t = APMIDI_DEBUT; !t.plusMinutes(DUREE_MIN).isAfter(APMIDI_FIN);
             t = t.plusMinutes(DUREE_MIN + REPOS_MIN)) creneaux.add(t);
        return Collections.unmodifiableList(creneaux);
    }

    //  STRUCTURE MÉMOIRE INTERNE
    private static class SlotMemoire {
        final long juryId, salleId;
        final LocalDate date;
        final LocalTime debut, fin;
        final Set<Long> profIds;

        SlotMemoire(long juryId, long salleId, LocalDate date,
                    LocalTime debut, LocalTime fin, Set<Long> profIds) {
            this.juryId  = juryId; this.salleId = salleId;
            this.date    = date;   this.debut   = debut;
            this.fin     = fin;    this.profIds = profIds;
        }
    }

    //  ALGORITHME PRINCIPAL DE PLANIFICATION
    @Override
    @Transactional
    public List<ResponseSoutenanceDTO> affecterSalles(LocalDate dateDebut,Long id) {
        ImportVersion version = versionRepository.findById(id).get();
        List<Jury>  tousJurys = juryRepository.findAllWithRelations(version);
        List<Salle> salles    = salleRepository.findByVersionAndDisponibleTrue(version);

        if (tousJurys.isEmpty())
            throw new IllegalStateException("Aucun jury trouvé.");
        if (salles.isEmpty())
            throw new IllegalStateException("Aucune salle disponible.");

        soutenanceRepository.deleteSoutenancesByVersion(version);

        List<LocalTime> creneauxJour = genererCreneauxJour();

        Map<Long, Integer> jurysParProf = calculerJurysParProf(tousJurys);
        List<Jury> remaining = tousJurys.stream()
                .sorted(Comparator.comparingInt((Jury j) ->
                        urgenceInitiale(j, jurysParProf)).reversed())
                .collect(Collectors.toList());

        Map<Long, Map<LocalDate, Integer>> quotaJour = new HashMap<>();
        Map<Long, SlotMemoire>             planning  = new LinkedHashMap<>();
        LocalDate jourCourant  = dateDebut;
        int       joursParcourus = 0;

        while (!remaining.isEmpty() && joursParcourus < MAX_JOURS_CAL) {

            while (jourCourant.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    jourCourant.getDayOfWeek() == DayOfWeek.SUNDAY)
                jourCourant = jourCourant.plusDays(1);

            final LocalDate jour = jourCourant;
            List<SlotMemoire> planningJour = planning.values().stream()
                    .filter(s -> s.date.equals(jour))
                    .collect(Collectors.toList());

            boolean progression;
            do {
                progression = false;
                for (LocalTime creneau : creneauxJour) {
                    if (remaining.isEmpty()) break;
                    LocalTime fin = creneau.plusMinutes(DUREE_MIN);

                    Set<Long> sallesOccupees = planningJour.stream()
                            .filter(s -> chevauchement(s.debut, s.fin, creneau, fin))
                            .map(s -> s.salleId)
                            .collect(Collectors.toSet());

                    for (Salle salle : salles) {
                        if (remaining.isEmpty()) break;
                        if (sallesOccupees.contains(salle.getId())) continue;

                        Jury jury = trouverMeilleurJury(
                                remaining, creneau, fin, planningJour, quotaJour);
                        if (jury == null) continue;

                        Set<Long>    pids = profIds(jury);
                        SlotMemoire  slot = new SlotMemoire(
                                jury.getId(), salle.getId(), jour, creneau, fin, pids);
                        planning.put(jury.getId(), slot);
                        planningJour.add(slot);
                        sallesOccupees.add(salle.getId());
                        remaining.remove(jury);
                        progression = true;
                        pids.forEach(pid -> quotaJour
                                .computeIfAbsent(pid, k -> new HashMap<>())
                                .merge(jour, 1, Integer::sum));
                    }
                }
            } while (progression && !remaining.isEmpty());

            jourCourant = jourCourant.plusDays(1);
            joursParcourus++;
        }

        if (!remaining.isEmpty())
            throw new IllegalStateException(String.format(
                    "Impossible de placer %d soutenance(s) après %d jours. Jurys : %s",
                    remaining.size(), joursParcourus,
                    remaining.stream().map(Jury::getId).collect(Collectors.toList())));

        // ── Persistance ───────────────────────────────────────────
        Map<Long, Jury>  juryById  = tousJurys.stream()
                .collect(Collectors.toMap(Jury::getId, j -> j));
        Map<Long, Salle> salleById = salles.stream()
                .collect(Collectors.toMap(Salle::getId, s -> s));

        List<Soutenance> aInserer = new ArrayList<>();
        for (SlotMemoire slot : planning.values()) {
            Jury  j = juryById.get(slot.juryId);
            Salle s = salleById.get(slot.salleId);
            aInserer.add(Soutenance.builder()
                    .pfe(j.getPfe()).jury(j).salle(s)
                    .dateSoutenance(slot.date)
                    .heureDebut(slot.debut)
                    .heureFin(slot.fin)
                    .version(version)
                    .build());
        }
        List<Soutenance> sauvegardees = soutenanceRepository.saveAll(aInserer);

        List<String> anomalies = detecterAnomalies(sauvegardees);
        if (!anomalies.isEmpty())
            System.err.println("⚠️ Anomalies :\n" + String.join("\n", anomalies));
        else
            System.out.println("✅ Planning OK (" + sauvegardees.size() + " soutenances).");

        return sauvegardees.stream().map(soutenanceMapper::toResponse).toList();
    }

    //  SÉLECTION DU MEILLEUR JURY
    private Jury trouverMeilleurJury(List<Jury> remaining,
                                     LocalTime debut, LocalTime fin,
                                     List<SlotMemoire> planningJour,
                                     Map<Long, Map<LocalDate, Integer>> quotaJour) {
        Jury bestJury  = null;
        int  bestScore = Integer.MAX_VALUE;
        for (Jury jury : remaining) {
            if (!juryDisponible(jury, debut, fin, planningJour)) continue;
            int score = getProfs(jury).stream()
                    .mapToInt(p -> quotaJour
                            .getOrDefault(p.getId(), Collections.emptyMap())
                            .values().stream().mapToInt(Integer::intValue).sum())
                    .sum();
            if (score < bestScore) { bestJury = jury; bestScore = score; }
        }
        return bestJury;
    }

    private boolean juryDisponible(Jury jury, LocalTime debut, LocalTime fin,
                                   List<SlotMemoire> planningJour) {
        for (Prof prof : getProfs(jury)) {
            for (SlotMemoire slot : planningJour) {
                if (!slot.profIds.contains(prof.getId())) continue;
                if (chevauchement(slot.debut, slot.fin, debut, fin)) return false;
                long g = gap(slot.debut, slot.fin, debut, fin);
                if (g >= 0 && g < REPOS_MIN) return false;
            }
        }
        return true;
    }

    //  DÉTECTION DES ANOMALIES
    public List<String> detecterAnomalies(List<Soutenance> soutenances) {
        List<String> anomalies = new ArrayList<>();


        for (int i = 0; i < soutenances.size(); i++) {
            Soutenance s1 = soutenances.get(i);

            // ④ Hors plage
            if (!dansPlagAutorisee(s1.getHeureDebut(), s1.getHeureFin()))
                anomalies.add(String.format("[HORAIRE] PFE '%s' hors plage : %s–%s",
                        s1.getPfe().getSujet(), s1.getHeureDebut(), s1.getHeureFin()));

            // ⑤ Week-end
            DayOfWeek dow = s1.getDateSoutenance().getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
                anomalies.add(String.format("[WEEK-END] PFE '%s' planifié un %s",
                        s1.getPfe().getSujet(), dow));

            // ⑥ Jury incomplet
            if (s1.getJury().getEncadrant() == null || s1.getJury().getProf1() == null)
                anomalies.add(String.format("[JURY INCOMPLET] Jury id=%d pour PFE '%s'",
                        s1.getJury().getId(), s1.getPfe().getSujet()));

            for (int j = i + 1; j < soutenances.size(); j++) {
                Soutenance s2 = soutenances.get(j);

                // ⑦ Doublon PFE
                if (s1.getPfe().getId().equals(s2.getPfe().getId()))
                    anomalies.add(String.format("[DOUBLON PFE] PFE '%s' planifié deux fois",
                            s1.getPfe().getSujet()));

                boolean memeJour = s1.getDateSoutenance().equals(s2.getDateSoutenance());
                if (!memeJour) continue;

                boolean overlap = chevauchement(s1.getHeureDebut(), s1.getHeureFin(),
                        s2.getHeureDebut(), s2.getHeureFin());

                // ① Chevauchement salle
                if (overlap && s1.getSalle().getId().equals(s2.getSalle().getId()))
                    anomalies.add(String.format(
                            "[SALLE] '%s' occupée deux fois le %s : %s–%s et %s–%s",
                            s1.getSalle().getNomSalle(), s1.getDateSoutenance(),
                            s1.getHeureDebut(), s1.getHeureFin(),
                            s2.getHeureDebut(), s2.getHeureFin()));

                Set<Long> communs = profIds(s1.getJury());
                communs.retainAll(profIds(s2.getJury()));
                if (communs.isEmpty()) continue;

                if (overlap)
                    // ② Conflit horaire
                    communs.forEach(pid -> anomalies.add(String.format(
                            "[CONFLIT] Prof id=%d : deux soutenances simultanées le %s",
                            pid, s1.getDateSoutenance())));
                else {
                    // ③ Repos insuffisant
                    long g = gap(s1.getHeureDebut(), s1.getHeureFin(),
                            s2.getHeureDebut(), s2.getHeureFin());
                    if (g >= 0 && g < REPOS_MIN)
                        communs.forEach(pid -> anomalies.add(String.format(
                                "[REPOS] Prof id=%d : %d min de repos le %s " +
                                        "entre %s–%s et %s–%s (minimum : %d min)",
                                pid, g, s1.getDateSoutenance(),
                                s1.getHeureDebut(), s1.getHeureFin(),
                                s2.getHeureDebut(), s2.getHeureFin(), REPOS_MIN)));
                }
            }
        }
        return anomalies;
    }

    //  EXPORT EXCEL
    @Override
    public byte[] exportPlanningExcel(Long versionId) throws IOException {
        ImportVersion version = versionRepository.findById(versionId).get();
        List<Soutenance> soutenances = soutenanceRepository
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version);
        XSSFWorkbook wb = new XSSFWorkbook();

        Map<String, String> profColorMap = buildProfColorMap(soutenances);
        Map<String, String> dateColorMap = buildDateColorMap(soutenances);

        ecrireFeuillePlanning(wb, soutenances, profColorMap, dateColorMap);
        ecrireFeuilleLegend(wb, profColorMap, dateColorMap);
        ecrireFeuilleAnomalies(wb, detecterAnomalies(soutenances));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out); wb.close();
        return out.toByteArray();
    }

    // ── Mapping prof → couleur (via ExcelTheme) ───────────────────
    private Map<String, String> buildProfColorMap(List<Soutenance> soutenances) {
        Set<String> profsSet = new TreeSet<>();
        for (Soutenance s : soutenances) {
            Jury j = s.getJury();
            if (j.getEncadrant() != null) profsSet.add(nomProf(j.getEncadrant()));
            if (j.getProf1()     != null) profsSet.add(nomProf(j.getProf1()));
            if (j.getProf2()     != null) profsSet.add(nomProf(j.getProf2()));
        }
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        for (String prof : profsSet)
            map.put(prof, ExcelTheme.PROF_PALETTE[i++ % ExcelTheme.PROF_PALETTE.length]);
        return map;
    }

    // ── Mapping date → couleur (via ExcelTheme) ───────────────────
    private Map<String, String> buildDateColorMap(List<Soutenance> soutenances) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        for (Soutenance s : soutenances) {
            String d = s.getDateSoutenance().toString();
            if (!map.containsKey(d))
                map.put(d, ExcelTheme.DATE_PALETTE[i++ % ExcelTheme.DATE_PALETTE.length]);
        }
        return map;
    }

    private String nomProf(Prof p) {
        return p.getNom() + " " + p.getPrenom();
    }

    //  FEUILLE PLANNING
    private void ecrireFeuillePlanning(XSSFWorkbook wb,
                                       List<Soutenance> soutenances,
                                       Map<String, String> profColorMap,
                                       Map<String, String> dateColorMap) {
        XSSFSheet     sheet  = wb.createSheet("Planning Soutenances");
        XSSFCellStyle header = buildHeaderStyle(wb);

        String[] cols = {"Date","Heure Début","Heure Fin","Salle","Filière",
                "Sujet PFE","Étudiant(s)","Encadrant","Membre 1","Membre 2"};
        Row h = sheet.createRow(0);
        h.setHeightInPoints(28);
        for (int i = 0; i < cols.length; i++) {
            Cell c = h.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(header);
        }

        int rowNum = 1;
        for (Soutenance s : soutenances) {
            Row  row  = sheet.createRow(rowNum);
            row.setHeightInPoints(30);
            Jury jury = s.getJury();

            String dateStr    = s.getDateSoutenance().toString();
            String heureStr   = s.getHeureDebut().toString().substring(0, 5);
            String filiereStr = s.getPfe().getEtudiants().stream()
                    .findFirst().map(e -> e.getFiliere().name()).orElse("");
            String etuds      = s.getPfe().getEtudiants().stream()
                    .map(e -> e.getNom() + " " + e.getPrenom())
                    .collect(Collectors.joining("\n"));
            String encNom    = jury.getEncadrant() != null ? nomProf(jury.getEncadrant()) : "";
            String p1Nom     = jury.getProf1()     != null ? nomProf(jury.getProf1())     : "";
            String p2Nom     = jury.getProf2()     != null ? nomProf(jury.getProf2())     : "N/A";
            String p2Affiche = jury.getProf2()     != null
                    ? p2Nom  : "N/A";

            // ── Styles via ExcelTheme ──────────────────────────────
            XSSFCellStyle dateStyle    = buildColorStyle(wb,
                    dateColorMap.getOrDefault(dateStr, "FFFFFF"), true);
            XSSFCellStyle timeStyle    = buildColorStyle(wb,
                    ExcelTheme.CRENEAU_COLORS.getOrDefault(heureStr, "FFFFFF"), true);
            XSSFCellStyle filiereStyle = buildColorStyle(wb,
                    ExcelTheme.FILIERE_COLORS.getOrDefault(filiereStr, "FFFFFF"), true);
            XSSFCellStyle rowStyle     = buildColorStyle(wb,
                    rowNum % 2 == 0 ? ExcelTheme.ROW_PAIR : ExcelTheme.ROW_IMPAIR, false);
            XSSFCellStyle encStyle     = buildColorStyle(wb,
                    profColorMap.getOrDefault(encNom, "FFFFFF"), false);
            XSSFCellStyle p1Style      = buildColorStyle(wb,
                    profColorMap.getOrDefault(p1Nom,  "FFFFFF"), false);
            XSSFCellStyle p2Style      = buildColorStyle(wb,
                    profColorMap.getOrDefault(p2Nom,  "FFFFFF"), false);

            ecrireCell(row, 0, dateStr,                                      dateStyle);
            ecrireCell(row, 1, heureStr,                                     timeStyle);
            ecrireCell(row, 2, s.getHeureFin().toString().substring(0, 5),   timeStyle);
            ecrireCell(row, 3, s.getSalle().getNomSalle(),                   rowStyle);
            ecrireCell(row, 4, filiereStr,                                   filiereStyle);
            ecrireCell(row, 5, s.getPfe().getSujet(),                        rowStyle);
            ecrireCell(row, 6, etuds,                                        rowStyle);
            ecrireCell(row, 7, encNom,                                       encStyle);
            ecrireCell(row, 8, p1Nom,                                        p1Style);
            ecrireCell(row, 9, p2Affiche,                                    p2Style);

            if (etuds.contains("\n")) {
                XSSFCellStyle wrap = wb.createCellStyle();
                wrap.cloneStyleFrom(rowStyle);
                wrap.setWrapText(true);
                row.getCell(6).setCellStyle(wrap);
                row.setHeightInPoints(45);
            }
            rowNum++;
        }

        int[] widths = {13,11,11,16,8,38,35,24,24,28};
        for (int i = 0; i < widths.length; i++)
            sheet.setColumnWidth(i, widths[i] * 256);
        sheet.createFreezePane(0, 1);
    }

    //  FEUILLE LÉGENDE
    private void ecrireFeuilleLegend(XSSFWorkbook wb,
                                     Map<String, String> profColorMap,
                                     Map<String, String> dateColorMap) {
        XSSFSheet     sheet = wb.createSheet("Légende");
        XSSFCellStyle hdr   = buildHeaderStyle(wb);

        // ── Profs (col 1-2) ────────────────────────────────────────
        ecrireLegendHeader(sheet, hdr, 1, 1, "Professeur");
        ecrireLegendHeader(sheet, hdr, 1, 2, "Couleur");
        int r = 2;
        for (Map.Entry<String, String> e : profColorMap.entrySet()) {
            Row row = sheet.createRow(r - 1);
            ecrireCell(row, 0, e.getKey(), buildColorStyle(wb, e.getValue(), false));
            ecrireCell(row, 1, "",          buildColorStyle(wb, e.getValue(), true));
            r++;
        }

        // ── Créneaux (col 4-5) ────────────────────────────────────
        ecrireLegendHeader(sheet, hdr, 1, 4, "Créneau");
        ecrireLegendHeader(sheet, hdr, 1, 5, "Couleur");
        r = 2;
        for (Map.Entry<String, String> e : ExcelTheme.CRENEAU_COLORS.entrySet()) {
            Row row = getOrCreateRow(sheet, r - 1);
            ecrireCell(row, 3, e.getKey(), buildColorStyle(wb, e.getValue(), true));
            ecrireCell(row, 4, "",          buildColorStyle(wb, e.getValue(), true));
            r++;
        }

        // ── Filières (col 7-8) ────────────────────────────────────
        ecrireLegendHeader(sheet, hdr, 1, 7, "Filière");
        ecrireLegendHeader(sheet, hdr, 1, 8, "Couleur");
        r = 2;
        for (Map.Entry<String, String> e : ExcelTheme.FILIERE_COLORS.entrySet()) {
            Row row = getOrCreateRow(sheet, r - 1);
            ecrireCell(row, 6, e.getKey(), buildColorStyle(wb, e.getValue(), true));
            ecrireCell(row, 7, "",          buildColorStyle(wb, e.getValue(), true));
            r++;
        }

        // ── Dates (col 10-11) ─────────────────────────────────────
        ecrireLegendHeader(sheet, hdr, 1, 10, "Date");
        ecrireLegendHeader(sheet, hdr, 1, 11, "Couleur");
        r = 2;
        for (Map.Entry<String, String> e : dateColorMap.entrySet()) {
            Row row = getOrCreateRow(sheet, r - 1);
            ecrireCell(row, 9,  e.getKey(), buildColorStyle(wb, e.getValue(), true));
            ecrireCell(row, 10, "",          buildColorStyle(wb, e.getValue(), true));
            r++;
        }

        int[] w = {26,10,3,14,10,3,10,10,3,16,10};
        for (int i = 0; i < w.length; i++)
            sheet.setColumnWidth(i, w[i] * 256);
    }

    private void ecrireLegendHeader(XSSFSheet sheet, XSSFCellStyle hdr,
                                    int row, int col, String label) {
        Row r = getOrCreateRow(sheet, row - 1);
        Cell c = r.createCell(col - 1);
        c.setCellValue(label);
        c.setCellStyle(hdr);
    }

    private Row getOrCreateRow(Sheet sheet, int index) {
        Row r = sheet.getRow(index);
        return r != null ? r : sheet.createRow(index);
    }

    //  FEUILLE ANOMALIES
    private void ecrireFeuilleAnomalies(XSSFWorkbook wb, List<String> anomalies) {
        XSSFSheet     sheet = wb.createSheet("Anomalies");
        XSSFCellStyle hdr   = buildHeaderStyle(wb);

        Cell c = sheet.createRow(0).createCell(0);
        c.setCellValue("Anomalies (" + anomalies.size() + ")");
        c.setCellStyle(hdr);
        sheet.setColumnWidth(0, 120 * 256);

        if (anomalies.isEmpty()) {
            Cell ok = sheet.createRow(1).createCell(0);
            ok.setCellValue("✔ Aucune anomalie — planning valide.");
            ok.setCellStyle(buildColorStyleWithFont(wb,
                    ExcelTheme.ANOMALIE_OK_BG, ExcelTheme.ANOMALIE_OK_FG));
        } else {
            for (int i = 0; i < anomalies.size(); i++) {
                Cell cell = sheet.createRow(i + 1).createCell(0);
                cell.setCellValue(anomalies.get(i));
                cell.setCellStyle(buildColorStyleWithFont(wb,
                        ExcelTheme.ANOMALIE_ERR_BG, ExcelTheme.ANOMALIE_ERR_FG));
            }
        }
    }

    //  IMPORT EXCEL — SALLES
    @Transactional
    @Override
    public List<ResponseSalleDTO> importFromExcel(Sheet sheet,ImportVersion version)  {
        if (sheet == null) throw new IllegalArgumentException("Feuille 'salles' introuvable.");
        List<Salle> nouvelles = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) continue;
            String nom = row.getCell(0).getStringCellValue().trim();
            int    cap = (int) row.getCell(1).getNumericCellValue();
            // Go back to this one 
            if (nom.isBlank() || salleRepository.existsByNomSalleAndVersion(nom, version)) continue;
            nouvelles.add(Salle.builder()
                    .nomSalle(nom).capacite(cap).disponible(true).version(version).build());
        }
        return nouvelles.isEmpty() ? Collections.emptyList()
                : salleRepository.saveAll(nouvelles).stream()
                .map(salleMapper::toResponse).toList();
    }

    //  IMPORT DATE DE DÉBUT
    @Override
    public LocalDate importDateDebut(Sheet sheet)  {
        if (sheet == null)
            throw new IllegalArgumentException("Feuille 'jours_soutenances' introuvable.");
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row  row  = sheet.getRow(i);
            if (row == null) continue;
            Cell cell = row.getCell(0);
            if (cell == null || cell.getCellType() == CellType.BLANK) continue;
            LocalDate date = (cell.getCellType() == CellType.NUMERIC
                    && DateUtil.isCellDateFormatted(cell))
                    ? cell.getLocalDateTimeCellValue().toLocalDate()
                    : LocalDate.parse(cell.getStringCellValue().trim());
            return date;
        }
        throw new IllegalArgumentException("Aucune date trouvée.");
    }

    //  STYLES — délèguent à ExcelTheme pour les couleurs
    private XSSFCellStyle buildHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 11);
        f.setColor(new XSSFColor(ExcelTheme.hexToBytes(ExcelTheme.HEADER_FG), null));
        s.setFont(f);
        s.setFillForegroundColor(
                new XSSFColor(ExcelTheme.hexToBytes(ExcelTheme.HEADER_BG), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private XSSFCellStyle buildColorStyle(XSSFWorkbook wb, String hex, boolean center) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(ExcelTheme.hexToBytes(hex), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private XSSFCellStyle buildColorStyleWithFont(XSSFWorkbook wb,
                                                  String hexBg, String hexFg) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontName("Arial");
        f.setFontHeightInPoints((short) 10);
        f.setColor(new XSSFColor(ExcelTheme.hexToBytes(hexFg), null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(ExcelTheme.hexToBytes(hexBg), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private void setBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private void ecrireCell(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val : "");
        cell.setCellStyle(style);
    }

    //  UTILITAIRES TEMPORELS
    private boolean chevauchement(LocalTime d1, LocalTime f1,
                                  LocalTime d2, LocalTime f2) {
        return d1.isBefore(f2) && d2.isBefore(f1);
    }

    private long gap(LocalTime d1, LocalTime f1, LocalTime d2, LocalTime f2) {
        if (!f1.isAfter(d2)) return min(d2) - min(f1);
        if (!f2.isAfter(d1)) return min(d1) - min(f2);
        return -1;
    }

    private long min(LocalTime t) { return t.getHour() * 60L + t.getMinute(); }

    private boolean dansPlagAutorisee(LocalTime debut, LocalTime fin) {
        return (!debut.isBefore(MATIN_DEBUT) && !fin.isAfter(MATIN_FIN))
                || (!debut.isBefore(APMIDI_DEBUT) && !fin.isAfter(APMIDI_FIN));
    }

    //  UTILITAIRES JURY / PROF
    private Map<Long, Integer> calculerJurysParProf(List<Jury> jurys) {
        Map<Long, Integer> map = new HashMap<>();
        jurys.forEach(j -> getProfs(j).forEach(p -> map.merge(p.getId(), 1, Integer::sum)));
        return map;
    }

    private int urgenceInitiale(Jury jury, Map<Long, Integer> jurysParProf) {
        return getProfs(jury).stream()
                .mapToInt(p -> jurysParProf.getOrDefault(p.getId(), 0))
                .max().orElse(0);
    }

    private List<Prof> getProfs(Jury jury) {
        List<Prof> l = new ArrayList<>();
        if (jury.getEncadrant() != null) l.add(jury.getEncadrant());
        if (jury.getProf1()     != null) l.add(jury.getProf1());
        if (jury.getProf2()     != null) l.add(jury.getProf2());
        return l;
    }

    private Set<Long> profIds(Jury jury) {
        Set<Long> ids = new HashSet<>();
        if (jury.getEncadrant() != null) ids.add(jury.getEncadrant().getId());
        if (jury.getProf1()     != null) ids.add(jury.getProf1().getId());
        if (jury.getProf2()     != null) ids.add(jury.getProf2().getId());
        return ids;
    }

    @Override
        public byte[] exportPlanningPDF(Long versionId) throws IOException {
        ImportVersion version = versionRepository.findById(versionId).get();
        List<Soutenance> soutenances = soutenanceRepository
                .findByVersionOrderByDateSoutenanceAscHeureDebutAscSalleNomSalleAsc(version);
        return PDFGenerator.exportPlanningPDF(soutenances);
        }
}
package org.sid.pfespring.services.scheduling;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;
import org.sid.pfespring.utils.ExcelTheme;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

/**
 * Génère les exports Excel et PDF d'une {@link SchedulingSolution}.
 *
 * Tri canonique : date ASC → heure début ASC → salle ASC
 * (même ordre que l'affichage à l'écran)
 *
 * Colonnes : Date | Début | Fin | Salle | Filière | Sujet PFE
 *            | Étudiant(s) | Encadrant | Membre 1 | Membre 2
 */
@Service
public class SchedulingExportServiceImpl implements SchedulingExportService {

    // ─── Tri canonique centralisé ─────────────────────────────────────────

    private static List<PlannedSoutenance> sorted(SchedulingSolution solution) {
        return solution.getSoutenancesPlanifiees().stream()
                .sorted(Comparator
                        .comparing((PlannedSoutenance ps) ->
                                ps.getSlot() != null ? ps.getSlot().getDate() : LocalDate.MAX)
                        .thenComparing(ps ->
                                ps.getSlot() != null ? ps.getSlot().getHeureDebut()
                                        : java.time.LocalTime.MAX)
                        .thenComparing(ps ->
                                ps.getSalle() != null ? ps.getSalle().getNomSalle() : ""))
                .toList();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  EXCEL
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public byte[] exportExcel(SchedulingSolution solution) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Planning Soutenances");
            int[] widths = {4500, 3000, 3000, 4500, 3000, 15000, 8000, 7500, 7500, 7500};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i]);

            Map<String, CellStyle> styles = buildExcelStyles(workbook);

            // En-tête
            Row header = sheet.createRow(0);
            header.setHeightInPoints(28);
            String[] cols = {"Date", "Début", "Fin", "Salle", "Filière",
                    "Sujet PFE", "Étudiant(s)", "Encadrant", "Membre 1", "Membre 2"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(styles.get("header"));
            }

            List<PlannedSoutenance> planifiees = sorted(solution);
            Map<String, String>     profColors = buildProfColorMap(planifiees);
            Map<String, String>     dateColors = buildDateColorMap(planifiees);

            int rowNum = 1;
            for (PlannedSoutenance ps : planifiees) {

                String dateStr  = ps.getSlot().getDate().toString();
                String debutStr = ps.getSlot().getHeureDebut().toString().substring(0, 5);
                String finStr   = ps.getSlot().getHeureFin().toString().substring(0, 5);
                String salle    = ps.getSalle().getNomSalle();
                String filiere  = filiere(ps);
                String sujet    = nvl(ps.getPfe().getSujet());
                String etuds    = etudiants(ps, ", ");
                String enc      = profNom(ps.getJury().getEncadrant());
                String prof1    = ps.getJury().getProf1() != null ? profNom(ps.getJury().getProf1()) : "—";
                String prof2    = ps.getJury().getProf2() != null ? profNom(ps.getJury().getProf2()) : "—";

                Row row = sheet.createRow(rowNum);
                row.setHeightInPoints(22);

                CellStyle dateSt = getOrCreate(styles, workbook, "date_" + dateStr,
                        dateColors.getOrDefault(dateStr, ExcelTheme.ROW_PAIR));
                CellStyle timeSt = getOrCreate(styles, workbook, "time_" + debutStr,
                        ExcelTheme.CRENEAU_COLORS.getOrDefault(debutStr, ExcelTheme.ROW_PAIR));
                CellStyle filSt  = getOrCreate(styles, workbook, "fil_" + filiere,
                        ExcelTheme.FILIERE_COLORS.getOrDefault(filiere, ExcelTheme.ROW_PAIR));
                CellStyle rowSt  = styles.get(rowNum % 2 == 0 ? "even" : "odd");
                CellStyle encSt  = getOrCreate(styles, workbook, "p_" + enc,
                        profColors.getOrDefault(enc, ExcelTheme.ROW_PAIR));
                CellStyle p1St   = getOrCreate(styles, workbook, "p_" + prof1,
                        profColors.getOrDefault(prof1, ExcelTheme.ROW_PAIR));
                CellStyle p2St   = getOrCreate(styles, workbook, "p_" + prof2,
                        profColors.getOrDefault(prof2, ExcelTheme.ROW_PAIR));

                set(row, 0, dateStr,  dateSt);
                set(row, 1, debutStr, timeSt);
                set(row, 2, finStr,   timeSt);
                set(row, 3, salle,    rowSt);
                set(row, 4, filiere,  filSt);
                set(row, 5, sujet,    rowSt);
                set(row, 6, etuds,    rowSt);
                set(row, 7, enc,      encSt);
                set(row, 8, prof1,    p1St);
                set(row, 9, prof2,    p2St);

                rowNum++;
            }

            addScoreSheet(workbook, solution);
            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void addScoreSheet(XSSFWorkbook wb, SchedulingSolution solution) {
        Sheet s = wb.createSheet("Score");
        s.setColumnWidth(0, 12000);
        s.setColumnWidth(1, 5000);
        int r = 0;
        row(s, r++, "Algorithme",         solution.getAlgorithme());
        row(s, r++, "Planifiées",          String.valueOf(solution.getScore().getPlanifiees()));
        row(s, r++, "Non planifiées",      String.valueOf(solution.getScore().getNonPlanifiees()));
        row(s, r++, "Taux de couverture",  pct(solution.getScore().tauxCouverture()));
        row(s, r++, "Score global",        pct(solution.getScore().getGlobal()));
        if (solution.getScore().getParCritere() != null) {
            for (Map.Entry<String, Double> e : solution.getScore().getParCritere().entrySet()) {
                row(s, r++, e.getKey(), pct(e.getValue()));
            }
        }
    }

    private void row(Sheet s, int r, String k, String v) {
        Row row = s.createRow(r);
        row.createCell(0).setCellValue(k);
        row.createCell(1).setCellValue(v);
    }

    private String pct(double v) { return String.format("%.1f%%", v * 100); }

    // ──────────────────────────────────────────────────────────────────────
    //  PDF
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public byte[] exportPDF(SchedulingSolution solution) throws IOException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Titre
            Font tf = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Planning des Soutenances PFE", tf);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            doc.add(title);

            // Sous-titre
            Font sf = new Font(Font.HELVETICA, 9, Font.ITALIC);
            Paragraph sub = new Paragraph(
                    String.format("Algorithme : %s  |  Planifiées : %d  |  Score : %.1f%%",
                            solution.getAlgorithme(),
                            solution.getScore().getPlanifiees(),
                            solution.getScore().getGlobal() * 100), sf);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(12);
            doc.add(sub);

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.2f, 1.4f, 1.4f, 2.2f, 1.7f, 4.5f, 3.5f, 3f, 3f, 3f});
            table.setHeaderRows(1);

            for (String h : new String[]{"Date","Début","Fin","Salle","Filière",
                    "Sujet PFE","Étudiant(s)","Encadrant","Membre 1","Membre 2"}) {
                pdfCell(table, h, hexC(ExcelTheme.HEADER_BG), true, 9, Font.BOLD);
            }

            List<PlannedSoutenance> planifiees = sorted(solution);
            Map<String, String>     profColors = buildProfColorMap(planifiees);
            Map<String, String>     dateColors = buildDateColorMap(planifiees);

            for (PlannedSoutenance ps : planifiees) {

                String dateStr  = ps.getSlot().getDate().toString();
                String debutStr = ps.getSlot().getHeureDebut().toString().substring(0, 5);
                String finStr   = ps.getSlot().getHeureFin().toString().substring(0, 5);
                String filiere  = filiere(ps);
                String etuds    = etudiants(ps, "\n");
                String enc      = profNom(ps.getJury().getEncadrant());
                String prof1    = ps.getJury().getProf1() != null ? profNom(ps.getJury().getProf1()) : "—";
                String prof2    = ps.getJury().getProf2() != null ? profNom(ps.getJury().getProf2()) : "—";

                Color rowBg = hexC(ExcelTheme.ROW_IMPAIR);

                pdfCell(table, dateStr,  hexC(dateColors.getOrDefault(dateStr, ExcelTheme.ROW_PAIR)), true, 7, Font.NORMAL);
                pdfCell(table, debutStr, hexC(ExcelTheme.CRENEAU_COLORS.getOrDefault(debutStr, ExcelTheme.ROW_PAIR)), true, 7, Font.NORMAL);
                pdfCell(table, finStr,   hexC(ExcelTheme.CRENEAU_COLORS.getOrDefault(debutStr, ExcelTheme.ROW_PAIR)), true, 7, Font.NORMAL);
                pdfCell(table, ps.getSalle().getNomSalle(), rowBg, false, 7, Font.NORMAL);
                pdfCell(table, filiere,  hexC(ExcelTheme.FILIERE_COLORS.getOrDefault(filiere, ExcelTheme.ROW_PAIR)), true, 7, Font.NORMAL);
                pdfCell(table, nvl(ps.getPfe().getSujet()), rowBg, false, 7, Font.NORMAL);
                pdfCell(table, etuds,    rowBg, false, 7, Font.NORMAL);
                pdfCell(table, enc,      hexC(profColors.getOrDefault(enc,  ExcelTheme.ROW_PAIR)), false, 7, Font.NORMAL);
                pdfCell(table, prof1,    hexC(profColors.getOrDefault(prof1, ExcelTheme.ROW_PAIR)), false, 7, Font.NORMAL);
                pdfCell(table, prof2,    hexC(profColors.getOrDefault(prof2, ExcelTheme.ROW_PAIR)), false, 7, Font.NORMAL);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        }
    }

    // ─── Helpers communs ──────────────────────────────────────────────────

    private String profNom(org.sid.pfespring.model.Prof p) {
        return p == null ? "—" : p.getNom() + " " + p.getPrenom();
    }

    private String filiere(PlannedSoutenance ps) {
        if (ps.getPfe() == null || ps.getPfe().getEtudiants() == null
                || ps.getPfe().getEtudiants().isEmpty()) return "";
        return ps.getPfe().getEtudiants().iterator().next().getFiliere().name();
    }

    private String etudiants(PlannedSoutenance ps, String sep) {
        if (ps.getPfe() == null || ps.getPfe().getEtudiants() == null) return "";
        return ps.getPfe().getEtudiants().stream()
                .map(e -> e.getNom() + " " + e.getPrenom())
                .collect(Collectors.joining(sep));
    }

    private String nvl(String s) { return s == null ? "" : s; }

    /** Construit une map prof → couleur en parcourant la liste triée. */
    private Map<String, String> buildProfColorMap(List<PlannedSoutenance> planifiees) {
        Set<String> profs = new TreeSet<>();
        for (PlannedSoutenance ps : planifiees) {
            if (ps.getJury().getEncadrant() != null) profs.add(profNom(ps.getJury().getEncadrant()));
            if (ps.getJury().getProf1()    != null) profs.add(profNom(ps.getJury().getProf1()));
            if (ps.getJury().getProf2()    != null) profs.add(profNom(ps.getJury().getProf2()));
        }
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        for (String p : profs) map.put(p, ExcelTheme.PROF_PALETTE[i++ % ExcelTheme.PROF_PALETTE.length]);
        return map;
    }

    /** Une couleur par date distincte, dans l'ordre d'apparition. */
    private Map<String, String> buildDateColorMap(List<PlannedSoutenance> planifiees) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        for (PlannedSoutenance ps : planifiees) {
            String d = ps.getSlot().getDate().toString();
            if (!map.containsKey(d))
                map.put(d, ExcelTheme.DATE_PALETTE[i++ % ExcelTheme.DATE_PALETTE.length]);
        }
        return map;
    }

    // ─── Excel styles ─────────────────────────────────────────────────────

    private Map<String, CellStyle> buildExcelStyles(XSSFWorkbook wb) {
        Map<String, CellStyle> styles = new HashMap<>();

        CellStyle h = wb.createCellStyle();
        h.setFillForegroundColor(new XSSFColor(ExcelTheme.hexToBytes(ExcelTheme.HEADER_BG), null));
        h.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font hf = wb.createFont();
        hf.setBold(true);
        hf.setColor(IndexedColors.WHITE.getIndex());
        hf.setFontHeightInPoints((short) 11);
        h.setFont(hf);
        h.setAlignment(HorizontalAlignment.CENTER);
        h.setVerticalAlignment(VerticalAlignment.CENTER);
        h.setBorderBottom(BorderStyle.THIN);
        h.setBorderTop(BorderStyle.THIN);
        h.setBorderLeft(BorderStyle.THIN);
        h.setBorderRight(BorderStyle.THIN);
        styles.put("header", h);

        styles.put("odd",  baseStyle(wb, ExcelTheme.ROW_IMPAIR));
        styles.put("even", baseStyle(wb, ExcelTheme.ROW_PAIR));
        return styles;
    }

    private CellStyle baseStyle(XSSFWorkbook wb, String hex) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(ExcelTheme.hexToBytes(hex), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setWrapText(true);
        return s;
    }

    private CellStyle getOrCreate(Map<String, CellStyle> styles, XSSFWorkbook wb,
                                  String key, String hex) {
        return styles.computeIfAbsent(key, k -> baseStyle(wb, hex));
    }

    private void set(Row row, int col, String v, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(v != null ? v : "");
        c.setCellStyle(style);
    }

    // ─── PDF helpers ──────────────────────────────────────────────────────

    private void pdfCell(PdfPTable table, String text, Color bg,
                         boolean center, int size, int style) {
        Font f = new Font(Font.HELVETICA, size, style,
                bg.equals(hexC(ExcelTheme.HEADER_BG)) ? Color.WHITE : Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", f));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(center ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private Color hexC(String hex) {
        if (hex == null || hex.length() < 6) return Color.WHITE;
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16));
    }
}
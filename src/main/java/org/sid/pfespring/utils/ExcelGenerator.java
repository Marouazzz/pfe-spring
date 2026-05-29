package org.sid.pfespring.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;

public class ExcelGenerator {

    public static byte[] exportPFEAffectationSheet(Map<String, Map<Long, String>> affectations) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("affectation_pfes");

        // Create styles first
        Map<String, CellStyle> styles = createStyles(workbook);

        // Calculer le nombre maximum d'étudiants par encadrant
        int maxEtudiants = 0;
        for (Map<Long, String> pfes : affectations.values()) {
            maxEtudiants = Math.max(maxEtudiants, pfes.size());
        }

        // Set column widths - TRÈS LARGES pour bien visualiser
        sheet.setColumnWidth(0, 8000);  // Encadrant

for (int i = 1; i <= maxEtudiants; i++) {

    int maxLength = 20;

    for (Map<Long, String> pfes : affectations.values()) {

        List<String> etudiantsList = new ArrayList<>(pfes.values());

        if (i - 1 < etudiantsList.size()) {

            String content = etudiantsList.get(i - 1);

            maxLength = Math.max(maxLength, content.length());
        }
    }

    sheet.setColumnWidth(i, Math.min((maxLength + 5) * 256, 12000));
}
        // Create HEADER row
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(30);

        // Header cell 0
        Cell headerCell0 = headerRow.createCell(0);
        headerCell0.setCellValue("ENCADRANT");
        headerCell0.setCellStyle(styles.get("header"));

        // Header cells for groups - Créer TOUS les groupes jusqu'à maxEtudiants
        for (int i = 1; i <= maxEtudiants; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue("GROUPE " + i);
            headerCell.setCellStyle(styles.get("header"));
        }

        // Fill data rows
        int rowNum = 1;
        for (String key : affectations.keySet()) {
            Row row = sheet.createRow(rowNum);
            row.setHeightInPoints(25);

            // Column 0: Encadrant
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(key);

            // Appliquer la couleur basée sur le nom de l'encadrant
            String encadrantColorKey = "prof_" + (Math.abs(key.hashCode()) % ExcelTheme.PROF_PALETTE.length);
            if (!styles.containsKey(encadrantColorKey)) {
                styles.put(encadrantColorKey, createProfStyle(workbook,
                        ExcelTheme.PROF_PALETTE[Math.abs(key.hashCode()) % ExcelTheme.PROF_PALETTE.length]));
            }
            cell0.setCellStyle(styles.get(encadrantColorKey));

            // Remplir les groupes - Convertir la Map en List pour avoir un ordre
            Map<Long, String> pfes = affectations.get(key);
            List<String> etudiantsList = new ArrayList<>(pfes.values());

            // Remplir les colonnes de groupes
            for (int colNum = 1; colNum <= maxEtudiants; colNum++) {
                Cell cell = row.createCell(colNum);

                // Si l'étudiant existe pour ce groupe, mettre son nom
if (colNum - 1 < etudiantsList.size()) {

    String etudiants = etudiantsList.get(colNum - 1)
            .replace(",", "\n");

    cell.setCellValue(etudiants);

    CellStyle wrapStyle = styles.get(
            rowNum % 2 == 0 ? "cell_even_wrap" : "cell_odd_wrap"
    );

    cell.setCellStyle(wrapStyle);

int nbEtudiants = etudiantsList.size();
row.setHeightInPoints(nbEtudiants * 20);
} else {
                    // Cellule vide pour les groupes sans étudiants
                    cell.setCellValue("");
                    CellStyle emptyStyle = styles.get(rowNum % 2 == 0 ? "cell_even_center" : "cell_odd_center");
                    cell.setCellStyle(emptyStyle);
                }
            }

            rowNum++;
        }

        // Freeze header row
        sheet.createFreezePane(0, 1);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            workbook.close();
            workbook.dispose();
            return out.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException("Erreur lors de la creation du fichier affectations_pfe", ioe);
        }
    }

    private static Map<String, CellStyle> createStyles(SXSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        byte[] headerBg = ExcelTheme.hexToBytes(ExcelTheme.HEADER_BG);
        XSSFColor headerColor = new XSSFColor(new java.awt.Color(
                headerBg[0] & 0xFF,
                headerBg[1] & 0xFF,
                headerBg[2] & 0xFF
        ), null);
        headerStyle.setFillForegroundColor(headerColor);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);

        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        styles.put("header", headerStyle);

        // Odd row styles
        CellStyle oddCenter = workbook.createCellStyle();
        oddCenter.setBorderBottom(BorderStyle.THIN);
        oddCenter.setBorderLeft(BorderStyle.THIN);
        oddCenter.setBorderRight(BorderStyle.THIN);
        oddCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        oddCenter.setAlignment(HorizontalAlignment.CENTER);
        oddCenter.setWrapText(true);
        byte[] oddBg = ExcelTheme.hexToBytes(ExcelTheme.ROW_IMPAIR);
        XSSFColor oddColor = new XSSFColor(new java.awt.Color(
                oddBg[0] & 0xFF,
                oddBg[1] & 0xFF,
                oddBg[2] & 0xFF
        ), null);
        oddCenter.setFillForegroundColor(oddColor);
        oddCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_odd_center", oddCenter);

        CellStyle oddWrap = workbook.createCellStyle();
        oddWrap.cloneStyleFrom(oddCenter);
        oddWrap.setAlignment(HorizontalAlignment.LEFT);
        styles.put("cell_odd_wrap", oddWrap);

        // Even row styles
        CellStyle evenCenter = workbook.createCellStyle();
        evenCenter.setBorderBottom(BorderStyle.THIN);
        evenCenter.setBorderLeft(BorderStyle.THIN);
        evenCenter.setBorderRight(BorderStyle.THIN);
        evenCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        evenCenter.setAlignment(HorizontalAlignment.CENTER);
        evenCenter.setWrapText(true);
        byte[] evenBg = ExcelTheme.hexToBytes(ExcelTheme.ROW_PAIR);
        XSSFColor evenColor = new XSSFColor(new java.awt.Color(
                evenBg[0] & 0xFF,
                evenBg[1] & 0xFF,
                evenBg[2] & 0xFF
        ), null);
        evenCenter.setFillForegroundColor(evenColor);
        evenCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_even_center", evenCenter);

        CellStyle evenWrap = workbook.createCellStyle();
        evenWrap.cloneStyleFrom(evenCenter);
        evenWrap.setAlignment(HorizontalAlignment.LEFT);
        styles.put("cell_even_wrap", evenWrap);

        return styles;
    }

    private static CellStyle createProfStyle(SXSSFWorkbook workbook, String hexColor) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setWrapText(true);

        byte[] bgBytes = ExcelTheme.hexToBytes(hexColor);
        XSSFColor bgColor = new XSSFColor(new java.awt.Color(
                bgBytes[0] & 0xFF,
                bgBytes[1] & 0xFF,
                bgBytes[2] & 0xFF
        ), null);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }
    
    
    public static byte[] exportPlanning(List<PlannedSoutenance> planifiees,SchedulingSolution solution) throws IOException {

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

    private static void addScoreSheet(XSSFWorkbook wb, SchedulingSolution solution) {
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

    private static void row(Sheet s, int r, String k, String v) {
        Row row = s.createRow(r);
        row.createCell(0).setCellValue(k);
        row.createCell(1).setCellValue(v);
    }

    private static String pct(double v) { return String.format("%.1f%%", v * 100); }


    private static Map<String, CellStyle> buildExcelStyles(XSSFWorkbook wb) {
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

        private static CellStyle baseStyle(XSSFWorkbook wb, String hex) {
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

    private static CellStyle getOrCreate(Map<String, CellStyle> styles, XSSFWorkbook wb,
                                  String key, String hex) {
        return styles.computeIfAbsent(key, k -> baseStyle(wb, hex));
    }

    private static void set(Row row, int col, String v, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(v != null ? v : "");
        c.setCellStyle(style);
    }

    
    // ─── Helpers communs ──────────────────────────────────────────────────

    private static String profNom(org.sid.pfespring.model.Prof p) {
        return p == null ? "—" : p.getNom() + " " + p.getPrenom();
    }

    private static String filiere(PlannedSoutenance ps) {
        if (ps.getPfe() == null || ps.getPfe().getEtudiants() == null
                || ps.getPfe().getEtudiants().isEmpty()) return "";
        return ps.getPfe().getEtudiants().iterator().next().getFiliere().name();
    }

    private static String etudiants(PlannedSoutenance ps, String sep) {
        if (ps.getPfe() == null || ps.getPfe().getEtudiants() == null) return "";
        return ps.getPfe().getEtudiants().stream()
                .map(e -> e.getNom() + " " + e.getPrenom())
                .collect(Collectors.joining(sep));
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    /** Construit une map prof → couleur en parcourant la liste triée. */
    private static Map<String, String> buildProfColorMap(List<PlannedSoutenance> planifiees) {
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
    private static Map<String, String> buildDateColorMap(List<PlannedSoutenance> planifiees) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        for (PlannedSoutenance ps : planifiees) {
            String d = ps.getSlot().getDate().toString();
            if (!map.containsKey(d))
                map.put(d, ExcelTheme.DATE_PALETTE[i++ % ExcelTheme.DATE_PALETTE.length]);
        }
        return map;
    }

}
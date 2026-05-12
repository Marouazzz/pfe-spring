package org.sid.pfespring.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

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
}
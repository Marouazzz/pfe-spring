package org.sid.pfespring.utils;

import java.awt.Color;
import java.io.ByteArrayOutputStream; // Explicitly use OpenPDF Font
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.sid.pfespring.model.scheduling.PlannedSoutenance;
import org.sid.pfespring.model.scheduling.SchedulingSolution;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PDFGenerator {
    public static byte[] exportPlanning(List<PlannedSoutenance> planifiees,SchedulingSolution solution) throws IOException {
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
    
    private static void pdfCell(PdfPTable table, String text, Color bg,
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

    private static Color hexC(String hex) {
        if (hex == null || hex.length() < 6) return Color.WHITE;
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16));
    }
}
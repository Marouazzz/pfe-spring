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

    // public static byte[] exportAffectationPDF(Map<String, Map<Long, String>> affectations) {

    //     try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
    //         // Create a document with A4 size
    //         Document document = new Document(PageSize.A4.rotate());
    //         PdfWriter.getInstance(document, out);

    //         document.open();

    //         // Define title with font style and size
    //         Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
    //         // Insert the title
    //         Paragraph title = new Paragraph("Affectation des PFEs pour l'annee universitaire ", titleFont);
    //         // Align it in the center of the page
    //         title.setAlignment(Element.ALIGN_CENTER);
    //         // Add it to the document
    //         title.setSpacingAfter(20);
    //         document.add(title);

    //         // Calculating the pfe with the maximum pfes in the promotion
    //         int maxPFEs = affectations.values()
    //                 .stream()
    //                 .mapToInt(map -> map.size())
    //                 .max()
    //                 .orElse(0);

    //         // Create a pdf table
    //         PdfPTable table = new PdfPTable(maxPFEs + 1);
    //         // The width equal to the whole page (excluding the margins ofc)
    //         table.setWidthPercentage(100);

    //         // Header row
    //         addHeaderCell(table, "ENCADRANT");

    //         for (int i = 1; i <= maxPFEs; i++) {
    //             addHeaderCell(table, "GROUPE " + i);
    //         }

    //         // Body 
    //         int rowIndex = 0;

    //         for (Map.Entry<String, Map<Long, String>> entry : affectations.entrySet()) {

    //             String encadrant = entry.getKey();
    //             List<String> pfes = entry.getValue()
    //                     .values()
    //                     .stream()
    //                     .collect(Collectors.toList()); 

    //             // creating the first cell with of the first row
    //             addBodyCell(table, encadrant, getProfColor(encadrant.hashCode()), true);

    //             // Students cells
    //             for (int i = 0; i < maxPFEs; i++) {
    //                 if (i < pfes.size()) {
    //                     addBodyCell(
    //                             table,
    //                             pfes.get(i), // Ajouter les etudiants
    //                             getRowColor(rowIndex),
    //                             false
    //                     );
    //                 } else {
    //                     addBodyCell(table, "", getRowColor(rowIndex), false);
    //                 }
    //             }
    //             rowIndex++;
    //         }

    //         // Add the table in the document
    //         document.add(table);
    //         // Close the document
    //         document.close();
    //         // return the document
    //         return out.toByteArray();

    //     } catch (Exception e) {
    //         throw new RuntimeException("Erreur génération PDF", e);
    //     }
    // }

    
    // private static void addHeaderCell(PdfPTable table, String text) {
    //     // Set the font of the heder row cells
    //     Font font = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    //     PdfPCell cell = new PdfPCell(new Phrase(text, font));

    //     cell.setBackgroundColor(hexToColor(ExcelTheme.HEADER_BG));
    //     cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    //     cell.setPadding(8);

    //     table.addCell(cell);
    // }

    // private static void addBodyCell(PdfPTable table, String text, Color bg, boolean bold) {
    //     Font font = new Font(Font.HELVETICA, 10, bold ? Font.BOLD : Font.NORMAL, Color.BLACK);
    //     PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));

    //     cell.setBackgroundColor(bg);
    //     cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    //     cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    //     cell.setPadding(6);

    //     table.addCell(cell);
    // }

    // public static byte[] exportJuryPDF(List<Jury> jurys) {
    //     try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    //         // Using Landscape A4 to accommodate the large number of columns
    //         Document document = new Document(PageSize.A4.rotate());
    //         PdfWriter.getInstance(document, out);

    //         document.open();

    //         // 1. Title
    //         Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
    //         Paragraph title = new Paragraph("Planning des Jurys PFE", titleFont);
    //         title.setAlignment(Element.ALIGN_CENTER);
    //         title.setSpacingAfter(20);
    //         document.add(title);

    //         // 2. Table Creation (6 columns based on your headers)
    //         PdfPTable table = new PdfPTable(6);
    //         table.setWidthPercentage(100);
    //         // Relative widths: N° is small, Subject is very large
    //         table.setWidths(new float[]{1f, 4f, 2f, 3f, 3f, 3f});

    //         // 3. Headers
    //         String[] headers = {"N°", "Sujet PFE", "CNEs", "Encadrant", "Professeur 1", "Professeur 2"};
    //         for (String header : headers) {
    //             addJuryHeaderCell(table, header);
    //         }

    //         // 4. Data Rows
    //         int count = 1;
    //         for (Jury jury : jurys) {
    //             // Determine Row Color (Pair/Impair)
    //             Color rowBg = (count % 2 == 0) 
    //                     ? hexToColor(ExcelTheme.ROW_PAIR) 
    //                     : hexToColor(ExcelTheme.ROW_IMPAIR);

    //             // Column 0: Index
    //             addJuryBodyCell(table, String.valueOf(count), rowBg, false);

    //             // Column 1: Subject
    //             addJuryBodyCell(table, jury.getPfe().getSujet(), rowBg, false);

    //             // Column 2: CNEs
    //             String cnes = jury.getPfe().getEtudiants().stream()
    //                     .map(etudiant -> etudiant.getCne())
    //                     .collect(Collectors.joining(", "));
    //             addJuryBodyCell(table, cnes, rowBg, false);

    //             // Column 3: Encadrant (with hash-based palette color)
    //             String encadrant = jury.getEncadrant().getNom() + " " + jury.getEncadrant().getPrenom();
    //             Color encColor = getProfColor(encadrant.hashCode());
    //             addJuryBodyCell(table, encadrant, encColor, true);

    //             // Column 4: Prof 1
    //             String prof1 = (jury.getProf1() != null) 
    //                     ? jury.getProf1().getNom() + " " + jury.getProf1().getPrenom() 
    //                     : "Non assigné";
    //             Color prof1Color = (jury.getProf1() != null) ? getProfColor(prof1.hashCode()) : rowBg;
    //             addJuryBodyCell(table, prof1, prof1Color, false);

    //             // Column 5: Prof 2
    //             String prof2 = (jury.getProf2() != null) 
    //                     ? jury.getProf2().getNom() + " " + jury.getProf2().getPrenom() 
    //                     : "Non assigné";
    //             Color prof2Color = (jury.getProf2() != null) ? getProfColor(prof2.hashCode()) : rowBg;
    //             addJuryBodyCell(table, prof2, prof2Color, false);

    //             count++;
    //         }

    //         document.add(table);
    //         document.close();
    //         return out.toByteArray();

    //     } catch (Exception e) {
    //         throw new RuntimeException("Erreur lors de la génération du PDF Jury", e);
    //     }
    // }

    // private static void addJuryHeaderCell(PdfPTable table, String text) {
    //     Font font = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    //     PdfPCell cell = new PdfPCell(new Phrase(text, font));
    //     cell.setBackgroundColor(hexToColor(ExcelTheme.HEADER_BG));
    //     cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    //     cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    //     cell.setPadding(8);
    //     table.addCell(cell);
    // }

    // private static void addJuryBodyCell(PdfPTable table, String text, Color bg, boolean bold) {
    //     Font font = new Font(Font.HELVETICA, 9, bold ? Font.BOLD : Font.NORMAL, Color.BLACK);
    //     PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
    //     cell.setBackgroundColor(bg);
    //     cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    //     cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    //     cell.setPadding(5);
    //     table.addCell(cell);
    // }

    // public static byte[] exportPlanningPDF(List<Soutenance> soutenances) {
    //     try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    //         // A4 Landscape to fit 10 columns
    //         Document document = new Document(PageSize.A4.rotate());
    //         PdfWriter.getInstance(document, out);
    //         document.open();

    //         // 1. Title
    //         Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
    //         Paragraph title = new Paragraph("Planning des Soutenances PFE", titleFont);
    //         title.setAlignment(Element.ALIGN_CENTER);
    //         title.setSpacingAfter(20);
    //         document.add(title);

    //         // 2. Pre-calculate Color Maps (Same logic as your Excel service)
    //         Map<String, String> profColorMap = buildProfColorMap(soutenances);
    //         Map<String, String> dateColorMap = buildDateColorMap(soutenances);

    //         // 3. Table Setup (10 Columns)
    //         PdfPTable table = new PdfPTable(10);
    //         table.setWidthPercentage(100);
    //         // Column widths optimized for content
    //         table.setWidths(new float[]{2.5f, 1.8f, 1.8f, 2.5f, 2f, 5f, 4f, 3.5f, 3.5f, 3.5f});

    //         // 4. Headers
    //         String[] headers = {"Date", "Début", "Fin", "Salle", "Filière", "Sujet PFE", "Étudiant(s)", "Encadrant", "Membre 1", "Membre 2"};
    //         for (String h : headers) {
    //             addPlanningHeaderCell(table, h);
    //         }

    //         // 5. Data Rows
    //         int rowNum = 1;
    //         for (Soutenance s : soutenances) {
    //             Jury jury = s.getJury();
    //             String dateStr = s.getDateSoutenance().toString();
    //             String startStr = s.getHeureDebut().toString().substring(0, 5);
    //             String endStr = s.getHeureFin().toString().substring(0, 5);
    //             String filiere = s.getPfe().getEtudiants().isEmpty() ? "" : s.getPfe().getEtudiants().stream().findFirst().map(e -> e.getFiliere().name()).orElse("N/A");
                
    //             String etudiants = s.getPfe().getEtudiants().stream()
    //                     .map(e -> e.getNom() + " " + e.getPrenom())
    //                     .collect(Collectors.joining("\n"));

    //             // Background Colors based on ExcelTheme
    //             Color dateBg = hexToColor(dateColorMap.getOrDefault(dateStr, "FFFFFF"));
    //             Color timeBg = hexToColor(ExcelTheme.CRENEAU_COLORS.getOrDefault(startStr, "FFFFFF"));
    //             Color filiereBg = hexToColor(ExcelTheme.FILIERE_COLORS.getOrDefault(filiere, "FFFFFF"));
    //             Color rowBg = hexToColor(rowNum % 2 == 0 ? ExcelTheme.ROW_PAIR : ExcelTheme.ROW_IMPAIR);

    //             // Add Cells
    //             addPlanningBodyCell(table, dateStr, dateBg, true);
    //             addPlanningBodyCell(table, startStr, timeBg, true);
    //             addPlanningBodyCell(table, endStr, timeBg, true);
    //             addPlanningBodyCell(table, s.getSalle().getNomSalle(), rowBg, false);
    //             addPlanningBodyCell(table, filiere, filiereBg, true);
    //             addPlanningBodyCell(table, s.getPfe().getSujet(), rowBg, false);
    //             addPlanningBodyCell(table, etudiants, rowBg, false);
                
    //             // Professor Columns with their specific colors
    //             String enc = jury.getEncadrant() != null ? (jury.getEncadrant().getNom() + " " + jury.getEncadrant().getPrenom()) : "";
    //             addPlanningBodyCell(table, enc, hexToColor(profColorMap.getOrDefault(enc, "FFFFFF")), false);

    //             String p1 = jury.getProf1() != null ? (jury.getProf1().getNom() + " " + jury.getProf1().getPrenom()) : "";
    //             addPlanningBodyCell(table, p1, hexToColor(profColorMap.getOrDefault(p1, "FFFFFF")), false);

    //             String p2 = jury.getProf2() != null ? (jury.getProf2().getNom() + " " + jury.getProf2().getPrenom()) : "N/A";
    //             addPlanningBodyCell(table, p2, hexToColor(profColorMap.getOrDefault(p2, "FFFFFF")), false);

    //             rowNum++;
    //         }

    //         document.add(table);
    //         document.close();
    //         return out.toByteArray();
    //     } catch (Exception e) {
    //         throw new RuntimeException("Error generating Planning PDF", e);
    //     }
    // }

    // // Helper Methods for Cell Styling
    // private static void addPlanningHeaderCell(PdfPTable table, String text) {
    //     Font font = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    //     PdfPCell cell = new PdfPCell(new Phrase(text, font));
    //     cell.setBackgroundColor(hexToColor(ExcelTheme.HEADER_BG));
    //     cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    //     cell.setPadding(6);
    //     table.addCell(cell);
    // }

    // private static void addPlanningBodyCell(PdfPTable table, String text, Color bg, boolean centered) {
    //     Font font = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    //     PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
    //     cell.setBackgroundColor(bg);
    //     cell.setHorizontalAlignment(centered ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
    //     cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    //     cell.setPadding(4);
    //     table.addCell(cell);
    // }

    // // Color Mapping Helpers (Replicating your Service logic)
    // private static Map<String, String> buildProfColorMap(List<Soutenance> soutenances) {
    //     Set<String> profs = new TreeSet<>();
    //     for (Soutenance s : soutenances) {
    //         if (s.getJury().getEncadrant() != null) profs.add(s.getJury().getEncadrant().getNom() + " " + s.getJury().getEncadrant().getPrenom());
    //         if (s.getJury().getProf1() != null) profs.add(s.getJury().getProf1().getNom() + " " + s.getJury().getProf1().getPrenom());
    //         if (s.getJury().getProf2() != null) profs.add(s.getJury().getProf2().getNom() + " " + s.getJury().getProf2().getPrenom());
    //     }
    //     Map<String, String> map = new HashMap<>();
    //     int i = 0;
    //     for (String p : profs) map.put(p, ExcelTheme.PROF_PALETTE[i++ % ExcelTheme.PROF_PALETTE.length]);
    //     return map;
    // }

    // private static Map<String, String> buildDateColorMap(List<Soutenance> soutenances) {
    //     Map<String, String> map = new HashMap<>();
    //     int i = 0;
    //     for (Soutenance s : soutenances) {
    //         String d = s.getDateSoutenance().toString();
    //         if (!map.containsKey(d)) map.put(d, ExcelTheme.DATE_PALETTE[i++ % ExcelTheme.DATE_PALETTE.length]);
    //     }
    //     return map;
    // }

    // =========================================================
    // COLORS
    // =========================================================

    // private static Color getRowColor(int index) {
    //     return hexToColor(index % 2 == 0
    //             ? ExcelTheme.ROW_PAIR
    //             : ExcelTheme.ROW_IMPAIR);
    // }

    // private static Color getProfColor(int hash) {
    //     String hex = ExcelTheme.PROF_PALETTE[Math.abs(hash) % ExcelTheme.PROF_PALETTE.length];
    //     return hexToColor(hex);
    // }

    // private static Color hexToColor(String hex) {
    //     // Handle potential # prefix if exists
    //     if (hex.startsWith("#")) hex = hex.substring(1);
        
    //     return new Color(
    //             Integer.valueOf(hex.substring(0, 2), 16),
    //             Integer.valueOf(hex.substring(2, 4), 16),
    //             Integer.valueOf(hex.substring(4, 6), 16)
    //     );
    // }


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
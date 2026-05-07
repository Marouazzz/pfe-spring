package org.sid.pfespring.utils;


import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.Map;

import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.ss.usermodel.Row;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ExcelGenerator {

    public static byte[] exportPFEAffectationSheet(Map<String,Map<Long,String>> affectations) throws IOException{
        SXSSFWorkbook workbook =  new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("affectation_pfes");
        // Max etudiants
        int maxEtudiants = affectations.values()
        .stream()
        .map(map -> map.size())
        .reduce((a,b) -> Math.max(a, b))
        .orElse(0);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Encadrant");
        for (int i=1;i<= maxEtudiants;i++){
            header.createCell(i).setCellValue("Groupe  " + i);
        }
        int i =1;
        for (String key : affectations.keySet()){
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(key);
            Map<Long,String>  pfes = affectations.get(key);
            int j=1;
            for(Long pfe: pfes.keySet()){
                row.createCell(j).setCellValue(pfes.get(pfe));
                j++;
            }
            i++;
        }

        try(ByteArrayOutputStream out = new ByteArrayOutputStream()){
            workbook.write(out);
            workbook.close();
            workbook.dispose();
            return out.toByteArray();
        }catch(IOException ioe){
            throw new RuntimeException("Erreur lors de la creation du fichier affectations_pfe");
        }
    } 


}

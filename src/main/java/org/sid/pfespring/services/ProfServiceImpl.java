package org.sid.pfespring.services;

import org.apache.poi.ss.usermodel.*;
import org.sid.pfespring.dto.ProfDTO;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.model.Specialite;
import org.sid.pfespring.repository.ProfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfServiceImpl implements ProfService {

    private final ProfRepository profRepository;

    public ProfServiceImpl(ProfRepository profRepository) {
        this.profRepository = profRepository;
    }

    @Override
    @Transactional
    public List<ProfDTO.Response> importFromExcel(MultipartFile file) {
        List<ProfDTO.Response> results = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {


            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nom       = formatter.formatCellValue(row.getCell(0)).trim();
                String prenom    = formatter.formatCellValue(row.getCell(1)).trim();
                String specialite = formatter.formatCellValue(row.getCell(2))
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_")   // "data ai" → "DATA_AI"
                        .replace("É", "E")   // "réseaux" → "RESEAUX"
                        .replace("È", "E")
                        .replace("Ê", "E")
                        .replace("Ç", "C")   // "français" → "FRANCAIS"
                        .replace("À", "A");

                // Skip lignes vides
                if (nom.isBlank() || specialite.isBlank()) continue;


                Prof prof = new Prof();
                prof.setNom(nom);
                prof.setPrenom(prenom);

                try {
                    prof.setSpecialite(Specialite.valueOf(specialite));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(
                            "Spécialité invalide à la ligne " + (i + 1) +
                                    " : '" + specialite + "'. Valeurs acceptées : " +
                                    java.util.Arrays.stream(Specialite.values())
                                            .map(Enum::name)
                                            .collect(java.util.stream.Collectors.joining(", "))
                    );
                }

                Prof saved = profRepository.save(prof);

                ProfDTO.Response dto = new ProfDTO.Response();
                dto.setId(saved.getId());
                dto.setNom(saved.getNom());
                dto.setPrenom(saved.getPrenom());
                dto.setSpecialite(saved.getSpecialite());
                results.add(dto);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier Excel : " + e.getMessage(), e);
        }

        return results;
    }
}
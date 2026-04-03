package org.sid.pfespring.services;

import org.apache.poi.ss.usermodel.*;
import org.sid.pfespring.dto.EtudiantDTO;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.Filiere;
import org.sid.pfespring.repository.EtudiantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;

    public EtudiantServiceImpl(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    @Override
    @Transactional
    public List<EtudiantDTO.Response> importFromExcel(MultipartFile file) {
        List<EtudiantDTO.Response> results = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(1);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cne    = formatter.formatCellValue(row.getCell(0)).trim();
                String nom    = formatter.formatCellValue(row.getCell(1)).trim();
                String prenom = formatter.formatCellValue(row.getCell(2)).trim();
                String filiere = formatter.formatCellValue(row.getCell(3)).trim();

                // Log temporaire — supprime après validation
                System.out.println("Row " + i + " → cne='" + cne + "' | filiere='" + filiere + "'");

                // Skip lignes vides
                if (cne.isBlank() || filiere.isBlank()) continue;

                Etudiant etudiant = new Etudiant();
                etudiant.setCne(cne);
                etudiant.setNom(nom);
                etudiant.setPrenom(prenom);

                try {
                    etudiant.setFiliere(Filiere.valueOf(filiere.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(
                            "Filière invalide à la ligne " + (i + 1) +
                                    " : '" + filiere + "'. Valeurs acceptées : " +
                                    java.util.Arrays.stream(Filiere.values())
                                            .map(Enum::name)
                                            .collect(java.util.stream.Collectors.joining(", "))
                    );
                }

                Etudiant saved = etudiantRepository.save(etudiant);

                EtudiantDTO.Response dto = new EtudiantDTO.Response();
                dto.setCne(saved.getCne());
                dto.setNom(saved.getNom());
                dto.setPrenom(saved.getPrenom());
                dto.setFiliere(saved.getFiliere());
                results.add(dto);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier Excel : " + e.getMessage(), e);
        }

        return results;
    }
}
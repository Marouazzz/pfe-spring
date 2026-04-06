package org.sid.pfespring.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.mapper.EtudiantMapper;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.repository.EtudiantRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

@Service
public class EtudiantServiceImpl extends AbstractService<
        Etudiant,
        RequestEtudiantDTO,
        ResponseEtudiantDTO> implements EtudiantService {

    public EtudiantServiceImpl(EtudiantRepository repository,
                               EtudiantMapper mapper) {
        super(repository, mapper);
    }

    @Override
    @Transactional
    public List<ResponseEtudiantDTO> importFromExcel(MultipartFile file) {

        List<ResponseEtudiantDTO> results = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(1);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cne = formatter.formatCellValue(row.getCell(0)).trim();
                String nom = formatter.formatCellValue(row.getCell(1)).trim();
                String prenom = formatter.formatCellValue(row.getCell(2)).trim();
                String filiere = formatter.formatCellValue(row.getCell(3))
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("É", "E")
                        .replace("È", "E")
                        .replace("Ê", "E")
                        .replace("Ç", "C")
                        .replace("À", "A");

                //  Skip lignes vides
                if (cne.isBlank() || nom.isBlank() || filiere.isBlank()) continue;

                //  Construire le DTO
                RequestEtudiantDTO request = new RequestEtudiantDTO(
                        cne,
                        nom,
                        prenom,
                        filiere
                );

                // Utiliser la logique générique
                ResponseEtudiantDTO saved = this.creer(request);

                results.add(saved);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture Excel: " + e.getMessage(), e);
        }

        return results;
    }
}

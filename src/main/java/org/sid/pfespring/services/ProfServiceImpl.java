package org.sid.pfespring.services;

import java.io.IOException;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.mapper.ProfMapper;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.repository.ProfRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;


@Service
public class ProfServiceImpl extends AbstractService<
        Prof,
        RequestProfDTO,
        ResponseProfDTO> implements ProfService {

    public ProfServiceImpl(ProfRepository repository,
                           ProfMapper mapper) {
        super(repository, mapper);
    }

    
    @Override
    @Transactional
    public void importFromExcel(MultipartFile file,ImportVersion version) {

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            Sheet sheet = workbook.getSheet("profs");
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nom = formatter.formatCellValue(row.getCell(0)).trim();
                String prenom = formatter.formatCellValue(row.getCell(1)).trim();
                String specialite = formatter.formatCellValue(row.getCell(2))
                        .trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("É", "E")
                        .replace("È", "E")
                        .replace("Ê", "E")
                        .replace("Ç", "C")
                        .replace("À", "A");

                //  Skip lignes vides
                if (nom.isBlank() || specialite.isBlank()) continue;

                //  Créer Request DTO
                RequestProfDTO request = new RequestProfDTO(
                        nom,
                        prenom,
                        specialite,
                        version

                );
                //  Utiliser le mapper + repository via service générique
                this.creer(request);
            }

        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture Excel: " + e.getMessage(), e);
        }
    }
}
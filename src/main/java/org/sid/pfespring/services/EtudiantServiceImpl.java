package org.sid.pfespring.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.sid.pfespring.exception.EtudiantImportValidationException;
import org.sid.pfespring.mapper.EtudiantMapper;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.repository.EtudiantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;


@Service
@Validated
public class EtudiantServiceImpl extends AbstractService<
        Etudiant,
        RequestEtudiantDTO,
        ResponseEtudiantDTO> implements EtudiantService {

        private Validator validator;

    public EtudiantServiceImpl(EtudiantRepository repository,
                               EtudiantMapper mapper,Validator validator) {
        super(repository, mapper);
        this.validator = validator;
    }

    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public  void importFromExcel(Sheet sheet ,ImportVersion version) {
            DataFormatter formatter = new DataFormatter();
            List<RequestEtudiantDTO> dtos= new ArrayList<>();
            List<String> errors = new ArrayList<>();

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
                        filiere,
                        version
                );
                Set<ConstraintViolation<RequestEtudiantDTO>> violations = validator.validate(request);
                if (!violations.isEmpty()) {
                        String rowErrors = violations.stream()
                        .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                        .collect(Collectors.joining(", "));
                        errors.add("Ligne " + (i + 1) + " -> " + rowErrors);
                }
                dtos.add(request);
            }

            if(!errors.isEmpty()){
                throw new EtudiantImportValidationException(errors);
            }

            List<Etudiant> etudiants = dtos.stream().map(mapper::toEntity).toList();
            this.repository.saveAll(etudiants);
    }

    
}

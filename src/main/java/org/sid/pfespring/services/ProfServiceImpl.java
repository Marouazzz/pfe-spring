package org.sid.pfespring.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.exception.ProfImportValidationException;
import org.sid.pfespring.mapper.ProfMapper;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Prof;
import org.sid.pfespring.repository.ProfRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;


@Service
@Validated
public class ProfServiceImpl extends AbstractService<
        Prof,
        RequestProfDTO,
        ResponseProfDTO> implements ProfService {


        private Validator validator;

    public ProfServiceImpl(ProfRepository repository,
                           ProfMapper mapper,
                           Validator validator) {
        super(repository, mapper);
        this.validator = validator;
    }

    
    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public void importFromExcel(Sheet sheet,ImportVersion version) {
            DataFormatter formatter = new DataFormatter();
            List <RequestProfDTO> dtos = new ArrayList<>();
            List<String> errors = new ArrayList<>();
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
                Set<ConstraintViolation<RequestProfDTO>> violations = validator.validate(request);

                if (!violations.isEmpty()) {
                        String rowErrors = violations.stream()
                        .map(v -> v.getPropertyPath() + " : " + v.getMessage())
                        .collect(Collectors.joining(", "));
                        errors.add("Ligne " + (i + 1) + " -> " + rowErrors);
                } 
                dtos.add(request);
            }
            if(!errors.isEmpty()){
                throw new ProfImportValidationException(errors);
            }
            List<Prof> profs = dtos.stream().map(mapper::toEntity).toList();
            repository.saveAll(profs);

    }
}
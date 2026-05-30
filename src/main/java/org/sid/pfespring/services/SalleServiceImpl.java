package org.sid.pfespring.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.dto.RequestSalleDTO;
import org.sid.pfespring.dto.ResponseSalleDTO;
import org.sid.pfespring.mapper.SalleMapper;
import org.sid.pfespring.mapper.SoutenanceMapper;
import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.model.Salle;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.sid.pfespring.repository.JuryRepository;
import org.sid.pfespring.repository.SalleRepository;
import org.sid.pfespring.repository.SoutenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalleServiceImpl
        extends AbstractService<Salle, RequestSalleDTO, ResponseSalleDTO>
        implements SalleService {


    private final SalleRepository      salleRepository;
    private final SalleMapper          salleMapper;

    public SalleServiceImpl(SalleRepository salleRepository,
                            SoutenanceRepository soutenanceRepository,
                            JuryRepository juryRepository,
                            ImportVersionRepository versionRepository,
                            SalleMapper salleMapper,
                            SoutenanceMapper soutenanceMapper) {
        super(salleRepository, salleMapper);
        this.salleRepository      = salleRepository;
        this.salleMapper          = salleMapper;
    }

    //  IMPORT EXCEL — SALLES
    @Transactional
    @Override
    public List<ResponseSalleDTO> importFromExcel(Sheet sheet,ImportVersion version)  {
        if (sheet == null) throw new IllegalArgumentException("Feuille 'salles' introuvable.");
        List<Salle> nouvelles = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getCell(0) == null) continue;
            String nom = row.getCell(0).getStringCellValue().trim();
            int    cap = (int) row.getCell(1).getNumericCellValue();
            // Go back to this one 
            if (nom.isBlank() || salleRepository.existsByNomSalleAndVersion(nom, version)) continue;
            nouvelles.add(Salle.builder()
                    .nomSalle(nom).capacite(cap).disponible(true).version(version).build());
        }
        return nouvelles.isEmpty() ? Collections.emptyList()
                : salleRepository.saveAll(nouvelles).stream()
                .map(salleMapper::toResponse).toList();
    }

    //  IMPORT DATE DE DÉBUT
    @Override
    public LocalDate importDateDebut(Sheet sheet)  {
        if (sheet == null)
            throw new IllegalArgumentException("Feuille 'jours_soutenances' introuvable.");
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row  row  = sheet.getRow(i);
            if (row == null) continue;
            Cell cell = row.getCell(0);
            if (cell == null || cell.getCellType() == CellType.BLANK) continue;
            LocalDate date = (cell.getCellType() == CellType.NUMERIC
                    && DateUtil.isCellDateFormatted(cell))
                    ? cell.getLocalDateTimeCellValue().toLocalDate()
                    : LocalDate.parse(cell.getStringCellValue().trim());
            return date;
        }
        throw new IllegalArgumentException("Aucune date trouvée.");
    }
}
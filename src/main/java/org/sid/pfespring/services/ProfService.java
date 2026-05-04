package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.model.ImportVersion;
import org.apache.poi.ss.usermodel.Sheet;

public interface ProfService extends GenericService<RequestProfDTO, ResponseProfDTO> {
    void importFromExcel(Sheet sheet,ImportVersion version);
}

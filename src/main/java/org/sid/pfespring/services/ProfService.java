package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.sid.pfespring.model.ImportVersion;
import org.springframework.web.multipart.MultipartFile;

public interface ProfService extends GenericService<RequestProfDTO, ResponseProfDTO> {
    void importFromExcel(MultipartFile file,ImportVersion version);
}

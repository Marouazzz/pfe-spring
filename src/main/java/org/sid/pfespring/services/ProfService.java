package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestProfDTO;
import org.sid.pfespring.dto.ResponseProfDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfService extends GenericService<RequestProfDTO, ResponseProfDTO> {
    List<ResponseProfDTO>importFromExcel(MultipartFile file);
}

package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PFEService extends GenericService<RequestPFEDTO, ResponsePFEDTO> {
    public int affecterProfPFE();
    public List <ResponsePFEDTO> importFromExcel(MultipartFile file);
}

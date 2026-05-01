package org.sid.pfespring.services;

import java.util.List;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.sid.pfespring.model.ImportVersion;

public interface PFEService extends GenericService<RequestPFEDTO, ResponsePFEDTO> {
    public void importFromExcel(MultipartFile file,ImportVersion version);
    public void appliquerAffectation(Long version);
    public byte[] exportPFEAffectation(Long version) throws IOException;
}

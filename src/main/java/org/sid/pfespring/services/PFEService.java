package org.sid.pfespring.services;

import java.util.List;

import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface PFEService extends GenericService<RequestPFEDTO, ResponsePFEDTO> {
    // public int affecterProfPFE();
    // public List <ResponsePFEDTO> importFromExcel(MultipartFile file);
    public List <RequestPFEDTO> readExcel(MultipartFile file);
    public void appliquerAffectation(List<RequestPFEDTO> pfesDto);
    public byte[] exportPFEAffectation() throws IOException;
}

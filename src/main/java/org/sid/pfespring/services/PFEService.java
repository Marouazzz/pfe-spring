package org.sid.pfespring.services;


import org.sid.pfespring.dto.RequestPFEDTO;
import org.sid.pfespring.dto.ResponsePFEDTO;
import org.apache.poi.ss.usermodel.Sheet;
import java.io.IOException;
import org.sid.pfespring.model.ImportVersion;

public interface PFEService extends GenericService<RequestPFEDTO, ResponsePFEDTO> {
    public void importFromExcel(Sheet sheet,ImportVersion version);
    public void appliquerAffectation(Long version);
    public byte[] exportPFEAffectation(Long version) throws IOException;
}

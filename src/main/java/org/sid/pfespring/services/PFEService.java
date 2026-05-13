package org.sid.pfespring.services;


import java.io.IOException;

import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.model.ImportVersion;

public interface PFEService {
    public void importFromExcel(Sheet sheet,ImportVersion version);
    public void appliquerAffectation(Long version);
    public byte[] exportPFEExcel(Long version) throws IOException;
    public byte[] exportPFEPDF(Long version) throws IOException;
    public void createPVFolder(Long id);
}

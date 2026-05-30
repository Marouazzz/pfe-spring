package org.sid.pfespring.services;


import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.model.ImportVersion;

public interface PFEService {
    public void importFromExcel(Sheet sheet,ImportVersion version);
    public void appliquerAffectation(Long version);
    public void createPVFolder(Long id);
}

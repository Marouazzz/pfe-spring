package org.sid.pfespring.services;

import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.model.ImportVersion;

public interface ProfService {
    void importFromExcel(Sheet sheet,ImportVersion version);
}

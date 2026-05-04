package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.sid.pfespring.model.ImportVersion;
public interface EtudiantService extends GenericService<RequestEtudiantDTO, ResponseEtudiantDTO>{

  void importFromExcel(Sheet sheet,ImportVersion version);

}

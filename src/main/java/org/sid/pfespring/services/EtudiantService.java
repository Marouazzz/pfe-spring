package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.springframework.web.multipart.MultipartFile;

public interface EtudiantService extends GenericService<RequestEtudiantDTO, ResponseEtudiantDTO>{

  void importFromExcel(MultipartFile file);

}

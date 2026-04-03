package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestEtudiantDTO;
import org.sid.pfespring.dto.ResponseEtudiantDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EtudiantService extends GenericService<RequestEtudiantDTO, ResponseEtudiantDTO>{

  List<ResponseEtudiantDTO> importFromExcel(MultipartFile file);

}

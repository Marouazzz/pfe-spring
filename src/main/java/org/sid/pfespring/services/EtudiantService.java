package org.sid.pfespring.services;

import org.sid.pfespring.dto.EtudiantDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface EtudiantService {

     List<EtudiantDTO.Response> importFromExcel(MultipartFile file);

}

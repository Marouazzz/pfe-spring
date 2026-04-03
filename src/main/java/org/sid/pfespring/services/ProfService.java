package org.sid.pfespring.services;

import org.sid.pfespring.dto.ProfDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfService {
    List<ProfDTO.Response>importFromExcel(MultipartFile file);
}

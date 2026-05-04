package org.sid.pfespring.services;

import org.sid.pfespring.dto.ResponseUploadDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    public ResponseUploadDTO importSheets(MultipartFile file);
}

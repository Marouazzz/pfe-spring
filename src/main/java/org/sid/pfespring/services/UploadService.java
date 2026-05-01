package org.sid.pfespring.services;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    public Long importSheets(MultipartFile file);
}

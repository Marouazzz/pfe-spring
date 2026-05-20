package org.sid.pfespring.services;

import org.sid.pfespring.model.ImportVersion;
import org.sid.pfespring.repository.ImportVersionRepository;
import org.springframework.stereotype.Service;

@Service
public class ImportVersionServiceImpl  implements  ImportVersionService{
    private ImportVersionRepository repository;

    

    public ImportVersionServiceImpl(ImportVersionRepository repository) {
        this.repository = repository;
    }

    @Override
    public ImportVersion addVersion() {
        ImportVersion version = new ImportVersion();
        this.repository.save(version);
        return version;
    }



}

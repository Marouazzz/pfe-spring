package org.sid.pfespring.services;

import java.io.IOException;

import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.Jury;
public interface FileSystemService {
    void createPVFolder(Encadrant encadrant);
    void generatePVFile(Jury jury);
    void deletePVFolder(Long id) throws IOException;
    public byte[] generateZip(Long versionId) throws IOException;
}

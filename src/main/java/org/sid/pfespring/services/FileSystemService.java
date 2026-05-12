package org.sid.pfespring.services;

import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.Jury;
import java.io.IOException;
public interface FileSystemService {
    void createPVFolder(Encadrant encadrant);
    void generatePVFile(Jury jury);
    void deletePVFolder(Long id) throws IOException;
}

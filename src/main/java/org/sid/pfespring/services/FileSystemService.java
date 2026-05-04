package org.sid.pfespring.services;

import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.Jury;

public interface FileSystemService {
    void createPVFolder(Encadrant encadrant);
    void generatePVFile(Jury jury);
}

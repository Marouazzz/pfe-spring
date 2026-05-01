package org.sid.pfespring.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import org.sid.pfespring.model.Encadrant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class FileSystemServiceImpl implements FileSystemService{


    @Value("${pv.root}")
    private String rootFolder;

    @Override
    public void createPVFolder(Encadrant encadrant) {   
        String filename = encadrant.getProf().getNom() + "_" + encadrant.getProf().getPrenom() + "_" +"v"+encadrant.getVersion().getId();
        Path path = Paths.get(rootFolder,filename);
        try {
            Files.createDirectories(path);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    
}

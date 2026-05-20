package org.sid.pfespring.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Text;
import org.sid.pfespring.model.Encadrant;
import org.sid.pfespring.model.Etudiant;
import org.sid.pfespring.model.Filiere;
import org.sid.pfespring.model.Jury;
import org.sid.pfespring.model.Prof;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

@Service
public class FileSystemServiceImpl implements FileSystemService{


    @Value("${pv.root}")
    private String rootFolder;

    @Value("${pv.template}")
    private Resource template;

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



    @Override
    public void generatePVFile(Jury jury) {
        try{
        File doc  = template.getFile();
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(doc);
        // get only the main document.xml
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        // Reading only text elements
        List<Object> texts = mainDocumentPart.getJAXBNodesViaXPath("//w:t", true);
        int i =0;
        List<Etudiant> etudiants = new ArrayList(jury.getPfe().getEtudiants());
        Prof encadrant = jury.getEncadrant();
        Prof prof1 = jury.getProf1();
        Prof prof2 = jury.getProf2();
        for (Object obj : texts) {
            Text textElement = (Text) ((JAXBElement)obj).getValue();
            String text = textElement.getValue();
            if (text.contains("${etudiant_" + (i + 1) + "}")) {
                if (i < etudiants.size()) {
                    text = text.replace("${etudiant_" + (i + 1) + "}",
                    etudiants.get(i).toString()
                );
                } else {
                    text = text.replace("${etudiant_" + (i + 1) + "}", "");
                }
            i++;
            }
            Filiere selected = jury.getPfe().getFiliere();
            for (Filiere filiere : Filiere.values()) {
                String placeholder = "${" + filiere.name() + "}";
                text = text.replace(
                    placeholder,
                    filiere.equals(selected) ? "☑" : "☐"
                );
            }
            text = text.replace("${titre}", jury.getPfe().getSujet());
            text = text.replace("${encadrant}", encadrant.getNom() + "\u00A0" + encadrant.getPrenom());
            text = text.replace("${prof_1}", prof1.getNom() + "\u00A0" + prof1.getPrenom());
            text = text.replace("${prof_2}", prof2.getNom() + "\u00A0" + prof2.getPrenom());
            textElement.setValue(text);
        }
        // Setting the correspending pv path 
        String PVFolder =encadrant.getNom()+"_"+encadrant.getPrenom()+"_v"+encadrant.getVersion().getId();
        String fileName = "PFE_"+jury.getId()+".docx";
        Path path = Paths.get(rootFolder,PVFolder,fileName);
        wordMLPackage.save(path.toFile());
        
    }catch(Docx4JException | IOException | JAXBException e){
        System.out.println(e.getMessage());
    }
    }

    @Override
    public void deletePVFolder(Long id) throws IOException {
    Path root = Paths.get(rootFolder);
    Files.list(root)
            .filter(Files::isDirectory)
            .filter(p -> p.getFileName().toString().endsWith("_v" + id))
            .forEach(profDir -> {
                try {
                    Files.walk(profDir)
                            .sorted((a, b) -> b.compareTo(a)) // delete files first
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
}

}

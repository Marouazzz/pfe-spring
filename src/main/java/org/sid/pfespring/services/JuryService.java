package org.sid.pfespring.services;

public interface JuryService  {
    void affecterJury(Long id);
    // byte[] exportJuryExcel(Long id) throws IOException;
    // byte[] exportJuryPDF(Long id) throws IOException;
    void genererPV(Long id);
}

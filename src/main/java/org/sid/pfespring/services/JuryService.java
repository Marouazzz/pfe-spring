package org.sid.pfespring.services;

import org.sid.pfespring.dto.RequestJuryDTO;
import org.sid.pfespring.dto.ResponseJuryDTO;

import java.io.IOException;
import java.util.List;

public interface JuryService extends GenericService<RequestJuryDTO, ResponseJuryDTO> {
    List<ResponseJuryDTO> affecterJury();
    byte[] exportJuryExcel() throws IOException;
}

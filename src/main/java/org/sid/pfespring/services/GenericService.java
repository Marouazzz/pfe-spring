package org.sid.pfespring.services;

import java.util.List;

public interface GenericService<Req, Res> {

    Res creer(Req request);

    List<Res> listerTous();
}
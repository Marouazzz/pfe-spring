package org.sid.pfespring.mapper;

import java.util.List;

public interface GenericMapper<E, Req, Res> {

    E toEntity(Req request);

    Res toResponse(E entity);

    List<Res> toResponseList(List<E> entities);
}
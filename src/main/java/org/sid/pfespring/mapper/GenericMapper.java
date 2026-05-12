package org.sid.pfespring.mapper;


public interface GenericMapper<E, Req, Res> {

    E toEntity(Req request);

    Res toResponse(E entity);
}
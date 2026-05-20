package org.sid.pfespring.services;

import org.sid.pfespring.mapper.GenericMapper;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AbstractService<E, Req, Res>{

    protected final JpaRepository<E, Long> repository;
    protected final GenericMapper<E, Req, Res> mapper;

    protected AbstractService(JpaRepository<E, Long> repository,
                              GenericMapper<E, Req, Res> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
package org.sid.pfespring.services;

import java.util.List;

import org.sid.pfespring.mapper.GenericMapper;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class AbstractService<E, Req, Res> implements GenericService<Req, Res> {

    protected final JpaRepository<E, Long> repository;
    protected final GenericMapper<E, Req, Res> mapper;

    protected AbstractService(JpaRepository<E, Long> repository,
                              GenericMapper<E, Req, Res> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Res creer(Req request) {
        E entity = mapper.toEntity(request);
        E saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public List<Res> listerTous() {
        return mapper.toResponseList(repository.findAll());
    }
}
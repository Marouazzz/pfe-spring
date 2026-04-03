package org.sid.pfespring.mapper;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMapper<E, Req, Res> implements GenericMapper<E, Req, Res> {

    @Override
    public List<Res> toResponseList(List<E> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
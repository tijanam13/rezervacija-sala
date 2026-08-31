package com.fon.rezervacija_sala.mapper;

import java.util.List;
import java.util.stream.Collectors;

public interface DtoEntityMapper<T, E> {

    T toDto(E e);

    E toEntity(T t);

    default List<T> toDtoList(List<E> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    default List<E> toEntityList(List<T> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

}
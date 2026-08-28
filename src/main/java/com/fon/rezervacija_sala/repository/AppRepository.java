package com.fon.rezervacija_sala.repository;

import java.util.List;
import java.util.Optional;

public interface AppRepository<E, ID> {

    List<E> findAll();

    Optional<E> findById(ID id);

    void save(E entity);

    void deleteById(ID id);

}
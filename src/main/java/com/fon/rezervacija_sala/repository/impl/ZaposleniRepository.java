package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Zaposleni;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ZaposleniRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Zaposleni> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Zaposleni.class, id));
    }

}
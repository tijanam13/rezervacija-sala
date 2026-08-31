package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.VerifikacioniToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class VerifikacioniTokenRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(VerifikacioniToken vt) {
        entityManager.persist(vt);
    }

    public VerifikacioniToken find(String token) {
        return entityManager.find(VerifikacioniToken.class, token);
    }

    @Transactional
    public void delete(VerifikacioniToken vt) {
        entityManager.remove(entityManager.contains(vt) ? vt : entityManager.merge(vt));
    }

}
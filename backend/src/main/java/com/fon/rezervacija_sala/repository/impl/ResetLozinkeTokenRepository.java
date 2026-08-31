package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.ResetLozinkeToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class ResetLozinkeTokenRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(ResetLozinkeToken t) {
        entityManager.persist(t);
    }

    public ResetLozinkeToken find(String token) {
        return entityManager.find(ResetLozinkeToken.class, token);
    }

    @Transactional
    public void delete(ResetLozinkeToken t) {
        entityManager.remove(entityManager.contains(t) ? t : entityManager.merge(t));
    }

}
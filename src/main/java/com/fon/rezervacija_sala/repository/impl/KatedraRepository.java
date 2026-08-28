package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class KatedraRepository implements AppRepository<Katedra, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Katedra> findAll() {
        return entityManager.createQuery("SELECT k FROM Katedra k", Katedra.class).getResultList();
    }

    @Override
    public Optional<Katedra> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Katedra.class, id));
    }

    @Override
    @Transactional
    public void save(Katedra entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Katedra k = entityManager.find(Katedra.class, id);
        if (k != null) {
            entityManager.remove(k);
        }
    }

}
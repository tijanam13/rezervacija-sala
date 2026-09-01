package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Sala;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SalaRepository implements AppRepository<Sala, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Sala> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT s FROM Sala s LEFT JOIN FETCH s.tipSale", Sala.class)
                .getResultList();
    }

    @Override
    public Optional<Sala> findById(Long id) {
        List<Sala> rez = entityManager.createQuery(
                "SELECT s FROM Sala s LEFT JOIN FETCH s.tipSale WHERE s.id = :id", Sala.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Sala entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Sala s = entityManager.find(Sala.class, id);
        if (s != null) {
            entityManager.remove(s);
        }
    }

    public Optional<Sala> findByIdForUpdate(Long id) {
        List<Sala> rez = entityManager.createQuery(
                "SELECT s FROM Sala s WHERE s.id = :id", Sala.class)
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

}
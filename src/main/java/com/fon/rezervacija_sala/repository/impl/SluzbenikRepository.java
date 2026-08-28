package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SluzbenikRepository implements AppRepository<Sluzbenik, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Sluzbenik> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT s FROM Sluzbenik s "
                + "LEFT JOIN FETCH s.sluzba", Sluzbenik.class)
                .getResultList();
    }

    @Override
    public Optional<Sluzbenik> findById(Long id) {
        List<Sluzbenik> rez = entityManager.createQuery(
                "SELECT s FROM Sluzbenik s "
                + "LEFT JOIN FETCH s.sluzba "
                + "WHERE s.id = :id", Sluzbenik.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Sluzbenik entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Sluzbenik s = entityManager.find(Sluzbenik.class, id);
        if (s != null) {
            entityManager.remove(s);
        }
    }

    public List<Sluzbenik> findBySluzba(Long sluzbaId) {
        return entityManager.createQuery(
                "SELECT s FROM Sluzbenik s "
                + "LEFT JOIN FETCH s.sluzba sl "
                + "WHERE sl.id = :sluzbaId", Sluzbenik.class)
                .setParameter("sluzbaId", sluzbaId)
                .getResultList();
    }

}
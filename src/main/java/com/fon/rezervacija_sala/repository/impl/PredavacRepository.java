package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PredavacRepository implements AppRepository<Predavac, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Predavac> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM Predavac p "
                + "LEFT JOIN FETCH p.katedra "
                + "LEFT JOIN FETCH p.zvanje", Predavac.class)
                .getResultList();
    }

    @Override
    public Optional<Predavac> findById(Long id) {
        List<Predavac> rez = entityManager.createQuery(
                "SELECT p FROM Predavac p "
                + "LEFT JOIN FETCH p.katedra "
                + "LEFT JOIN FETCH p.zvanje "
                + "WHERE p.id = :id", Predavac.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Predavac entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Predavac p = entityManager.find(Predavac.class, id);
        if (p != null) {
            entityManager.remove(p);
        }
    }

    public List<Predavac> findByKatedra(Long katedraId) {
        return entityManager.createQuery(
                "SELECT p FROM Predavac p "
                + "LEFT JOIN FETCH p.katedra k "
                + "LEFT JOIN FETCH p.zvanje "
                + "WHERE k.id = :katedraId", Predavac.class)
                .setParameter("katedraId", katedraId)
                .getResultList();
    }

}
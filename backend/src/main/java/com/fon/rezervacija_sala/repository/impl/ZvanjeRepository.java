package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ZvanjeRepository implements AppRepository<Zvanje, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Zvanje> findAll() {
        return entityManager.createQuery("SELECT z FROM Zvanje z", Zvanje.class).getResultList();
    }

    @Override
    public Optional<Zvanje> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Zvanje.class, id));
    }

    @Override
    @Transactional
    public void save(Zvanje entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Zvanje z = entityManager.find(Zvanje.class, id);
        if (z != null) {
            entityManager.remove(z);
        }
    }

    public Optional<Zvanje> findByNaziv(String naziv) {
        List<Zvanje> rez = entityManager.createQuery(
                "SELECT z FROM Zvanje z WHERE z.naziv = :naziv", Zvanje.class)
                .setParameter("naziv", naziv)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

}
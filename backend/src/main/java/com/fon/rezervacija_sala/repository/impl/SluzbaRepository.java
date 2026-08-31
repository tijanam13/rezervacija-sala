package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class SluzbaRepository implements AppRepository<Sluzba, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Sluzba> findAll() {
        return entityManager.createQuery("SELECT s FROM Sluzba s", Sluzba.class).getResultList();
    }

    @Override
    public Optional<Sluzba> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Sluzba.class, id));
    }

    @Override
    @Transactional
    public void save(Sluzba entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Sluzba s = entityManager.find(Sluzba.class, id);
        if (s != null) {
            entityManager.remove(s);
        }
    }

    public Optional<Sluzba> findByNaziv(String naziv) {
        List<Sluzba> rez = entityManager.createQuery(
                "SELECT s FROM Sluzba s WHERE s.naziv = :naziv", Sluzba.class)
                .setParameter("naziv", naziv)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

}
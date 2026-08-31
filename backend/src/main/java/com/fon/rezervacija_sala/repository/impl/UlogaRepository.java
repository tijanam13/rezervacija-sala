package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.entity.Uloga;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UlogaRepository implements AppRepository<Uloga, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Uloga> findAll() {
        return entityManager.createQuery("SELECT u FROM Uloga u", Uloga.class).getResultList();
    }

    @Override
    public Optional<Uloga> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Uloga.class, id));
    }

    @Override
    @Transactional
    public void save(Uloga entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Uloga u = entityManager.find(Uloga.class, id);
        if (u != null) {
            entityManager.remove(u);
        }
    }

    public Optional<Uloga> findByNaziv(NazivUloge naziv) {
        List<Uloga> rez = entityManager.createQuery(
                "SELECT u FROM Uloga u WHERE u.naziv = :naziv", Uloga.class)
                .setParameter("naziv", naziv)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

}
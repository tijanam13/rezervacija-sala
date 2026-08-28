package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.StavkaRezervacije;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class StavkaRezervacijeRepository implements AppRepository<StavkaRezervacije, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<StavkaRezervacije> findAll() {
        return entityManager.createQuery(
                "SELECT st FROM StavkaRezervacije st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale", StavkaRezervacije.class)
                .getResultList();
    }

    @Override
    public Optional<StavkaRezervacije> findById(Long id) {
        List<StavkaRezervacije> rez = entityManager.createQuery(
                "SELECT st FROM StavkaRezervacije st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale "
                + "WHERE st.id = :id", StavkaRezervacije.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(StavkaRezervacije entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        StavkaRezervacije st = entityManager.find(StavkaRezervacije.class, id);
        if (st != null) {
            entityManager.remove(st);
        }
    }

}
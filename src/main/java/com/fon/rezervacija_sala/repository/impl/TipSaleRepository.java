package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.TipSale;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TipSaleRepository implements AppRepository<TipSale, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<TipSale> findAll() {
        return entityManager.createQuery("SELECT t FROM TipSale t", TipSale.class).getResultList();
    }

    @Override
    public Optional<TipSale> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TipSale.class, id));
    }

    @Override
    @Transactional
    public void save(TipSale entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        TipSale t = entityManager.find(TipSale.class, id);
        if (t != null) {
            entityManager.remove(t);
        }
    }

}
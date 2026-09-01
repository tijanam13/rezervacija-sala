package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.StatusStavke;
import com.fon.rezervacija_sala.entity.StavkaRezervacije;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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

    public List<StavkaRezervacije> findAktivneStavkeZaPeriod(LocalDate od, LocalDate doDatum) {
        return entityManager.createQuery(
                "SELECT st FROM StavkaRezervacije st "
                + "JOIN FETCH st.sala "
                + "JOIN FETCH st.rezervacija r "
                + "JOIN FETCH r.svrha "
                + "WHERE st.datumTermina BETWEEN :od AND :doDatum "
                + "AND st.statusStavke IN :aktivniStatusi", StavkaRezervacije.class)
                .setParameter("od", od)
                .setParameter("doDatum", doDatum)
                .setParameter("aktivniStatusi", List.of(StatusStavke.NA_CEKANJU, StatusStavke.ODOBRENA))
                .getResultList();
    }

    public List<StavkaRezervacije> findIstekleNaCekanju(LocalDate danas) {
        return entityManager.createQuery(
                "SELECT st FROM StavkaRezervacije st "
                + "JOIN FETCH st.rezervacija "
                + "WHERE st.statusStavke = com.fon.rezervacija_sala.entity.StatusStavke.NA_CEKANJU "
                + "AND st.datumTermina < :danas", StavkaRezervacije.class)
                .setParameter("danas", danas)
                .getResultList();
    }

    public long brojStavkiZaSalu(Long salaId) {
        return entityManager.createQuery(
                "SELECT COUNT(st) FROM StavkaRezervacije st WHERE st.sala.id = :salaId", Long.class)
                .setParameter("salaId", salaId)
                .getSingleResult();
    }

}
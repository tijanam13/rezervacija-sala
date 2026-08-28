package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Rezervacija;
import com.fon.rezervacija_sala.entity.StatusRezervacije;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RezervacijaRepository implements AppRepository<Rezervacija, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Rezervacija> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT r FROM Rezervacija r "
                + "LEFT JOIN FETCH r.stavke st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale", Rezervacija.class)
                .getResultList();
    }

    @Override
    public Optional<Rezervacija> findById(Long id) {
        List<Rezervacija> rez = entityManager.createQuery(
                "SELECT r FROM Rezervacija r "
                + "LEFT JOIN FETCH r.stavke st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale "
                + "WHERE r.idRezervacije = :id", Rezervacija.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Rezervacija entity) {
        if (entity.getIdRezervacije() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Rezervacija r = entityManager.find(Rezervacija.class, id);
        if (r != null) {
            entityManager.remove(r);
        }
    }

    public List<Rezervacija> findByKorisnikId(Long korisnikId) {
        return entityManager.createQuery(
                "SELECT DISTINCT r FROM Rezervacija r "
                + "LEFT JOIN FETCH r.stavke st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale "
                + "WHERE r.korisnik.id = :korisnikId", Rezervacija.class)
                .setParameter("korisnikId", korisnikId)
                .getResultList();
    }

    public List<Rezervacija> findByStatus(StatusRezervacije status) {
        return entityManager.createQuery(
                "SELECT DISTINCT r FROM Rezervacija r "
                + "LEFT JOIN FETCH r.stavke st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale "
                + "WHERE r.status = :status", Rezervacija.class)
                .setParameter("status", status)
                .getResultList();
    }

    public boolean postojiPreklapanje(Long salaId, LocalDate datum,
            LocalTime vremeOd, LocalTime vremeDo, Long iskljuciStavkuId) {

        String jpql = "SELECT COUNT(st) FROM StavkaRezervacije st "
                + "WHERE st.sala.id = :salaId "
                + "AND st.datumTermina = :datum "
                + "AND st.statusStavke NOT IN (com.fon.rezervacija_sala.entity.StatusStavke.OTKAZANA, "
                + "com.fon.rezervacija_sala.entity.StatusStavke.ODBIJENA) "
                + "AND st.vremeOd < :vremeDo AND st.vremeDo > :vremeOd"
                + (iskljuciStavkuId != null ? " AND st.id <> :iskljuciStavkuId" : "");

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class)
                .setParameter("salaId", salaId)
                .setParameter("datum", datum)
                .setParameter("vremeOd", vremeOd)
                .setParameter("vremeDo", vremeDo);

        if (iskljuciStavkuId != null) {
            query.setParameter("iskljuciStavkuId", iskljuciStavkuId);
        }

        return query.getSingleResult() > 0;
    }

}
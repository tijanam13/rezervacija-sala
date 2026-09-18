package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Rezervacija;
import com.fon.rezervacija_sala.entity.StatusRezervacije;
import com.fon.rezervacija_sala.entity.StavkaRezervacije;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
                + "WHERE r.id = :id", Rezervacija.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Rezervacija entity) {
        if (entity.getId() == null) {
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

    public int zauzetoOsobaUTerminu(Long salaId, LocalDate datum,
            LocalTime vremeOd, LocalTime vremeDo, Long iskljuciStavkuId) {

        String jpql = "SELECT COALESCE(SUM(st.brojOsoba), 0) FROM StavkaRezervacije st "
                + "WHERE st.sala.id = :salaId "
                + "AND st.rezervacija.datumTermina = :datum "
                + "AND st.statusStavke NOT IN (com.fon.rezervacija_sala.entity.StatusStavke.OTKAZANA, "
                + "com.fon.rezervacija_sala.entity.StatusStavke.ODBIJENA) "
                + "AND st.rezervacija.vremeOd < :vremeDo AND st.rezervacija.vremeDo > :vremeOd"
                + (iskljuciStavkuId != null ? " AND st.id <> :iskljuciStavkuId" : "");

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class)
                .setParameter("salaId", salaId)
                .setParameter("datum", datum)
                .setParameter("vremeOd", vremeOd)
                .setParameter("vremeDo", vremeDo);

        if (iskljuciStavkuId != null) {
            query.setParameter("iskljuciStavkuId", iskljuciStavkuId);
        }

        return query.getSingleResult().intValue();
    }

    public List<StavkaRezervacije> pronadjiPreklapajuceStavke(Long salaId, LocalDate datum,
            LocalTime vremeOd, LocalTime vremeDo, Long iskljuciStavkuId) {

        String jpql = "SELECT st FROM StavkaRezervacije st "
                + "JOIN FETCH st.rezervacija r "
                + "JOIN FETCH r.svrha "
                + "WHERE st.sala.id = :salaId "
                + "AND r.datumTermina = :datum "
                + "AND st.statusStavke NOT IN (com.fon.rezervacija_sala.entity.StatusStavke.OTKAZANA, "
                + "com.fon.rezervacija_sala.entity.StatusStavke.ODBIJENA) "
                + "AND r.vremeOd < :vremeDo AND r.vremeDo > :vremeOd"
                + (iskljuciStavkuId != null ? " AND st.id <> :iskljuciStavkuId" : "");

        TypedQuery<StavkaRezervacije> query = entityManager.createQuery(jpql, StavkaRezervacije.class)
                .setParameter("salaId", salaId)
                .setParameter("datum", datum)
                .setParameter("vremeOd", vremeOd)
                .setParameter("vremeDo", vremeDo);

        if (iskljuciStavkuId != null) {
            query.setParameter("iskljuciStavkuId", iskljuciStavkuId);
        }

        return query.getResultList();
    }

    public List<Rezervacija> findAllPaged(int stranica, int velicina) {
        List<Long> ids = entityManager.createQuery(
                "SELECT r.id FROM Rezervacija r "
                + "ORDER BY r.datumTermina ASC, r.vremeOd ASC", Long.class)
                .setFirstResult(stranica * velicina)
                .setMaxResults(velicina)
                .getResultList();
        return ucitajPunoPremaIdjevima(ids);
    }

    public long brojSvihRezervacija() {
        return entityManager.createQuery("SELECT COUNT(r) FROM Rezervacija r", Long.class).getSingleResult();
    }

    public List<Rezervacija> findByStatusPaged(StatusRezervacije status, int stranica, int velicina) {
        List<Long> ids = entityManager.createQuery(
                "SELECT r.id FROM Rezervacija r WHERE r.status = :status "
                + "ORDER BY r.datumTermina ASC, r.vremeOd ASC", Long.class)
                .setParameter("status", status)
                .setFirstResult(stranica * velicina)
                .setMaxResults(velicina)
                .getResultList();
        return ucitajPunoPremaIdjevima(ids);
    }

    public long brojRezervacijaPoStatusu(StatusRezervacije status) {
        return entityManager.createQuery(
                "SELECT COUNT(r) FROM Rezervacija r WHERE r.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public List<Rezervacija> findByKorisnikIdPaged(Long korisnikId, StatusRezervacije status,
            LocalDate odDatum, LocalDate doDatum, int stranica, int velicina) {
        String jpql = "SELECT r.id FROM Rezervacija r WHERE r.korisnik.id = :korisnikId "
                + (status != null ? "AND r.status = :status " : "")
                + (odDatum != null ? "AND r.datumTermina >= :odDatum " : "")
                + (doDatum != null ? "AND r.datumTermina <= :doDatum " : "")
                + "ORDER BY r.datumTermina ASC";

        var upit = entityManager.createQuery(jpql, Long.class)
                .setParameter("korisnikId", korisnikId);
        if (status != null) {
            upit.setParameter("status", status);
        }
        if (odDatum != null) {
            upit.setParameter("odDatum", odDatum);
        }
        if (doDatum != null) {
            upit.setParameter("doDatum", doDatum);
        }

        List<Long> ids = upit
                .setFirstResult(stranica * velicina)
                .setMaxResults(velicina)
                .getResultList();
        return ucitajPunoPremaIdjevima(ids);
    }

    public long brojRezervacijaPoKorisniku(Long korisnikId, StatusRezervacije status,
            LocalDate odDatum, LocalDate doDatum) {
        String jpql = "SELECT COUNT(r) FROM Rezervacija r WHERE r.korisnik.id = :korisnikId "
                + (status != null ? "AND r.status = :status " : "")
                + (odDatum != null ? "AND r.datumTermina >= :odDatum " : "")
                + (doDatum != null ? "AND r.datumTermina <= :doDatum " : "");

        var upit = entityManager.createQuery(jpql, Long.class)
                .setParameter("korisnikId", korisnikId);
        if (status != null) {
            upit.setParameter("status", status);
        }
        if (odDatum != null) {
            upit.setParameter("odDatum", odDatum);
        }
        if (doDatum != null) {
            upit.setParameter("doDatum", doDatum);
        }
        return upit.getSingleResult();
    }

    private List<Rezervacija> ucitajPunoPremaIdjevima(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Rezervacija> rez = entityManager.createQuery(
                "SELECT DISTINCT r FROM Rezervacija r "
                + "LEFT JOIN FETCH r.stavke st "
                + "LEFT JOIN FETCH st.sala sa "
                + "LEFT JOIN FETCH sa.tipSale "
                + "WHERE r.id IN :ids", Rezervacija.class)
                .setParameter("ids", ids)
                .getResultList();

        Map<Long, Rezervacija> poId = rez.stream()
                .collect(Collectors.toMap(Rezervacija::getId, r -> r));

        return ids.stream().map(poId::get).filter(Objects::nonNull).toList();
    }

}
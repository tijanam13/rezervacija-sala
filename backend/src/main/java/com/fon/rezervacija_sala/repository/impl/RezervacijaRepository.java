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

        return query.getSingleResult().intValue();
    }

    public List<StavkaRezervacije> pronadjiPreklapajuceStavke(Long salaId, LocalDate datum,
            LocalTime vremeOd, LocalTime vremeDo, Long iskljuciStavkuId) {

        String jpql = "SELECT st FROM StavkaRezervacije st "
                + "JOIN FETCH st.rezervacija r "
                + "JOIN FETCH r.svrha "
                + "WHERE st.sala.id = :salaId "
                + "AND st.datumTermina = :datum "
                + "AND st.statusStavke NOT IN (com.fon.rezervacija_sala.entity.StatusStavke.OTKAZANA, "
                + "com.fon.rezervacija_sala.entity.StatusStavke.ODBIJENA) "
                + "AND st.vremeOd < :vremeDo AND st.vremeDo > :vremeOd"
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
                "SELECT r.idRezervacije FROM Rezervacija r JOIN r.stavke st "
                + "GROUP BY r.idRezervacije ORDER BY MIN(st.datumTermina) ASC", Long.class)
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
                "SELECT r.idRezervacije FROM Rezervacija r JOIN r.stavke st WHERE r.status = :status "
                + "GROUP BY r.idRezervacije ORDER BY MIN(st.datumTermina) ASC", Long.class)
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

    public List<Rezervacija> findByKorisnikIdPaged(Long korisnikId, int stranica, int velicina) {
        List<Long> ids = entityManager.createQuery(
                "SELECT r.idRezervacije FROM Rezervacija r WHERE r.korisnik.id = :korisnikId "
                + "ORDER BY r.datumKreiranja DESC", Long.class)
                .setParameter("korisnikId", korisnikId)
                .setFirstResult(stranica * velicina)
                .setMaxResults(velicina)
                .getResultList();
        return ucitajPunoPremaIdjevima(ids);
    }

    public long brojRezervacijaPoKorisniku(Long korisnikId) {
        return entityManager.createQuery(
                "SELECT COUNT(r) FROM Rezervacija r WHERE r.korisnik.id = :korisnikId", Long.class)
                .setParameter("korisnikId", korisnikId)
                .getSingleResult();
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
                + "WHERE r.idRezervacije IN :ids", Rezervacija.class)
                .setParameter("ids", ids)
                .getResultList();

        Map<Long, Rezervacija> poId = rez.stream()
                .collect(Collectors.toMap(Rezervacija::getIdRezervacije, r -> r));

        return ids.stream().map(poId::get).filter(Objects::nonNull).toList();
    }

}
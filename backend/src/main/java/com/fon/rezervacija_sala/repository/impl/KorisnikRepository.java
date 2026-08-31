package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class KorisnikRepository implements AppRepository<Korisnik, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Korisnik> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT k FROM Korisnik k LEFT JOIN FETCH k.zaposleni", Korisnik.class)
                .getResultList();
    }

    public List<Korisnik> findAllPaged(int stranica, int velicina, String pretraga) {
        String jql = "SELECT DISTINCT k FROM Korisnik k LEFT JOIN FETCH k.zaposleni z"
                + (imaTekstPretrage(pretraga) ? " WHERE " + uslovPretrage() : "")
                + " ORDER BY k.email";

        var upit = entityManager.createQuery(jql, Korisnik.class);
        if (imaTekstPretrage(pretraga)) {
            upit.setParameter("pretraga", "%" + pretraga.trim().toLowerCase() + "%");
        }

        return upit
                .setFirstResult(stranica * velicina)
                .setMaxResults(velicina)
                .getResultList();
    }

    public long brojSvihKorisnika(String pretraga) {
        String jql = "SELECT COUNT(DISTINCT k) FROM Korisnik k LEFT JOIN k.zaposleni z"
                + (imaTekstPretrage(pretraga) ? " WHERE " + uslovPretrage() : "");

        var upit = entityManager.createQuery(jql, Long.class);
        if (imaTekstPretrage(pretraga)) {
            upit.setParameter("pretraga", "%" + pretraga.trim().toLowerCase() + "%");
        }

        return upit.getSingleResult();
    }

    private boolean imaTekstPretrage(String pretraga) {
        return pretraga != null && !pretraga.isBlank();
    }

    private String uslovPretrage() {
        return "LOWER(z.ime) LIKE :pretraga OR LOWER(z.prezime) LIKE :pretraga "
                + "OR LOWER(k.email) LIKE :pretraga "
                + "OR LOWER(CONCAT(z.ime, ' ', z.prezime)) LIKE :pretraga";
    }

    @Override
    public Optional<Korisnik> findById(Long id) {
        List<Korisnik> rez = entityManager.createQuery(
                "SELECT k FROM Korisnik k LEFT JOIN FETCH k.zaposleni WHERE k.id = :id", Korisnik.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Korisnik entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Korisnik k = entityManager.find(Korisnik.class, id);
        if (k != null) {
            entityManager.remove(k);
        }
    }

    public Optional<Korisnik> findByEmail(String email) {
        List<Korisnik> rez = entityManager.createQuery(
                "SELECT k FROM Korisnik k LEFT JOIN FETCH k.zaposleni WHERE k.email = :email", Korisnik.class)
                .setParameter("email", email)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    public Optional<Korisnik> findByZaposleniId(Long zaposleniId) {
        List<Korisnik> rez = entityManager.createQuery(
                "SELECT k FROM Korisnik k LEFT JOIN FETCH k.zaposleni WHERE k.zaposleni.id = :zaposleniId",
                Korisnik.class)
                .setParameter("zaposleniId", zaposleniId)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    public long brojKorisnikaSaUlogom(NazivUloge naziv) {
        return entityManager.createQuery(
                "SELECT COUNT(k) FROM Korisnik k JOIN k.uloge u WHERE u.naziv = :naziv", Long.class)
                .setParameter("naziv", naziv)
                .getSingleResult();
    }

}
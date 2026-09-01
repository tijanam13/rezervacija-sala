package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.repository.AppRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PredavacRepository implements AppRepository<Predavac, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Predavac> findAll() {
        return entityManager.createQuery(
                "SELECT DISTINCT p FROM Predavac p "
                + "LEFT JOIN FETCH p.katedra "
                + "LEFT JOIN FETCH p.zvanje", Predavac.class)
                .getResultList();
    }

    @Override
    public Optional<Predavac> findById(Long id) {
        List<Predavac> rez = entityManager.createQuery(
                "SELECT p FROM Predavac p "
                + "LEFT JOIN FETCH p.katedra "
                + "LEFT JOIN FETCH p.zvanje "
                + "WHERE p.id = :id", Predavac.class)
                .setParameter("id", id)
                .getResultList();
        return rez.isEmpty() ? Optional.empty() : Optional.of(rez.get(0));
    }

    @Override
    @Transactional
    public void save(Predavac entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Predavac p = entityManager.find(Predavac.class, id);
        if (p != null) {
            entityManager.remove(p);
        }
    }

    public long brojPredavacaZaKatedru(Long katedraId) {
        return entityManager.createQuery(
                "SELECT COUNT(p) FROM Predavac p WHERE p.katedra.id = :katedraId", Long.class)
                .setParameter("katedraId", katedraId)
                .getSingleResult();
    }

    public long brojPredavacaZaZvanje(Long zvanjeId) {
        return entityManager.createQuery(
                "SELECT COUNT(p) FROM Predavac p WHERE p.zvanje.id = :zvanjeId", Long.class)
                .setParameter("zvanjeId", zvanjeId)
                .getSingleResult();
    }
    
    public boolean jeReferenciranKaoMentorIliKomisija(Long predavacId) {
        Long brojKaoMentor = entityManager.createQuery(
                "SELECT COUNT(z) FROM ZavrsniRad z WHERE z.mentor.id = :predavacId", Long.class)
                .setParameter("predavacId", predavacId)
                .getSingleResult();
        Long brojKaoClanKomisije = entityManager.createQuery(
                "SELECT COUNT(z) FROM ZavrsniRad z JOIN z.clanoviKomisije c WHERE c.id = :predavacId", Long.class)
                .setParameter("predavacId", predavacId)
                .getSingleResult();
        return brojKaoMentor > 0 || brojKaoClanKomisije > 0;
    }

}
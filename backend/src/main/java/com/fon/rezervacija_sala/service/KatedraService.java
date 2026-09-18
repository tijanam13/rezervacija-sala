package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.KatedraDto;
import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.exception.BrisanjeNijeMoguceException;
import com.fon.rezervacija_sala.exception.NazivZauzetException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.KatedraMapper;
import com.fon.rezervacija_sala.repository.impl.KatedraRepository;
import com.fon.rezervacija_sala.repository.impl.PredavacRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KatedraService {

    private static final Logger log = LoggerFactory.getLogger(KatedraService.class);

    private final KatedraRepository katedre;
    private final PredavacRepository predavci;
    private final KatedraMapper mapper;

    public KatedraService(KatedraRepository katedre, PredavacRepository predavci, KatedraMapper mapper) {
        this.katedre = katedre;
        this.predavci = predavci;
        this.mapper = mapper;
    }

    public List<KatedraDto> findAll() {
        return mapper.toDtoList(katedre.findAll());
    }

    @Transactional
    public KatedraDto create(KatedraDto dto) {
        proveriDaNazivNijeZauzet(dto.getNaziv(), null);
        Katedra k = mapper.toEntity(dto);
        k.setId(null); 
        katedre.save(k);
        log.info("Kreirana katedra (id: {}, naziv: {}).", k.getId(), k.getNaziv());
        return mapper.toDto(k);
    }

    @Transactional
    public KatedraDto update(Long id, KatedraDto dto) {
        Katedra postojeca = pronadjiIliBaciGresku(id);
        proveriDaNazivNijeZauzet(dto.getNaziv(), id);
        postojeca.setNaziv(dto.getNaziv());
        postojeca.setOpis(dto.getOpis());
        katedre.save(postojeca);
        log.info("Ažurirana katedra (id: {}).", id);
        return mapper.toDto(postojeca);
    }

    @Transactional
    public void deleteById(Long id) {
        Katedra k = pronadjiIliBaciGresku(id);

        long brojPredavaca = predavci.brojPredavacaZaKatedru(id);
        if (brojPredavaca > 0) {
            throw new BrisanjeNijeMoguceException(
                    "Katedra \"" + k.getNaziv() + "\" se ne može obrisati, vezana je za "
                    + brojPredavaca + " predavača.");
        }

        katedre.deleteById(id);
        log.info("Obrisana katedra (id: {}).", id);
    }

    private Katedra pronadjiIliBaciGresku(Long id) {
        return katedre.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Katedra ne postoji."));
    }

    private void proveriDaNazivNijeZauzet(String naziv, Long trenutniId) {
        katedre.findByNaziv(naziv).ifPresent(postojeca -> {
            if (!postojeca.getId().equals(trenutniId)) {
                throw new NazivZauzetException("Katedra sa ovim nazivom već postoji.");
            }
        });
    }

}
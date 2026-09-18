package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.ZvanjeDto;
import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.exception.BrisanjeNijeMoguceException;
import com.fon.rezervacija_sala.exception.NazivZauzetException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.ZvanjeMapper;
import com.fon.rezervacija_sala.repository.impl.PredavacRepository;
import com.fon.rezervacija_sala.repository.impl.ZvanjeRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZvanjeService {

    private static final Logger log = LoggerFactory.getLogger(ZvanjeService.class);

    private final ZvanjeRepository zvanja;
    private final PredavacRepository predavci;
    private final ZvanjeMapper mapper;

    public ZvanjeService(ZvanjeRepository zvanja, PredavacRepository predavci, ZvanjeMapper mapper) {
        this.zvanja = zvanja;
        this.predavci = predavci;
        this.mapper = mapper;
    }

    public List<ZvanjeDto> findAll() {
        return mapper.toDtoList(zvanja.findAll());
    }

    @Transactional
    public ZvanjeDto create(ZvanjeDto dto) {
        proveriDaNazivNijeZauzet(dto.getNaziv(), null);
        Zvanje z = mapper.toEntity(dto);
        z.setId(null);
        zvanja.save(z);
        log.info("Kreirano zvanje (id: {}, naziv: {}).", z.getId(), z.getNaziv());
        return mapper.toDto(z);
    }

    @Transactional
    public ZvanjeDto update(Long id, ZvanjeDto dto) {
        Zvanje postojece = pronadjiIliBaciGresku(id);
        proveriDaNazivNijeZauzet(dto.getNaziv(), id);
        postojece.setNaziv(dto.getNaziv());
        postojece.setOpis(dto.getOpis());
        zvanja.save(postojece);
        log.info("Ažurirano zvanje (id: {}).", id);
        return mapper.toDto(postojece);
    }

    @Transactional
    public void deleteById(Long id) {
        Zvanje z = pronadjiIliBaciGresku(id);

        long brojPredavaca = predavci.brojPredavacaZaZvanje(id);
        if (brojPredavaca > 0) {
            throw new BrisanjeNijeMoguceException(
                    "Zvanje \"" + z.getNaziv() + "\" se ne može obrisati, vezano je za "
                    + brojPredavaca + " predavača.");
        }

        zvanja.deleteById(id);
        log.info("Obrisano zvanje (id: {}).", id);
    }

    private Zvanje pronadjiIliBaciGresku(Long id) {
        return zvanja.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Zvanje ne postoji."));
    }

    private void proveriDaNazivNijeZauzet(String naziv, Long trenutniId) {
        zvanja.findByNaziv(naziv).ifPresent(postojece -> {
            if (!postojece.getId().equals(trenutniId)) {
                throw new NazivZauzetException("Zvanje sa ovim nazivom već postoji.");
            }
        });
    }

}
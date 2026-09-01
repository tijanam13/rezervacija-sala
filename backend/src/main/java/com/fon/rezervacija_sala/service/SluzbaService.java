package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.SluzbaDto;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.exception.BrisanjeNijeMoguceException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.SluzbaMapper;
import com.fon.rezervacija_sala.repository.impl.SluzbaRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbenikRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SluzbaService {

    private static final Logger log = LoggerFactory.getLogger(SluzbaService.class);

    private final SluzbaRepository sluzbe;
    private final SluzbenikRepository sluzbenici;
    private final SluzbaMapper mapper;

    public SluzbaService(SluzbaRepository sluzbe, SluzbenikRepository sluzbenici, SluzbaMapper mapper) {
        this.sluzbe = sluzbe;
        this.sluzbenici = sluzbenici;
        this.mapper = mapper;
    }

    public List<SluzbaDto> findAll() {
        return mapper.toDtoList(sluzbe.findAll());
    }

    @Transactional
    public SluzbaDto create(SluzbaDto dto) {
        Sluzba s = mapper.toEntity(dto);
        s.setId(null);
        sluzbe.save(s);
        log.info("Kreirana služba (id: {}, naziv: {}).", s.getId(), s.getNaziv());
        return mapper.toDto(s);
    }

    @Transactional
    public SluzbaDto update(Long id, SluzbaDto dto) {
        Sluzba postojeca = pronadjiIliBaciGresku(id);
        postojeca.setNaziv(dto.getNaziv());
        postojeca.setOpis(dto.getOpis());
        sluzbe.save(postojeca);
        log.info("Ažurirana služba (id: {}).", id);
        return mapper.toDto(postojeca);
    }

    @Transactional
    public void deleteById(Long id) {
        Sluzba s = pronadjiIliBaciGresku(id);

        long brojSluzbenika = sluzbenici.brojSluzbenikaZaSluzbu(id);
        if (brojSluzbenika > 0) {
            throw new BrisanjeNijeMoguceException(
                    "Služba \"" + s.getNaziv() + "\" se ne može obrisati, vezana je za "
                    + brojSluzbenika + " službenika.");
        }

        sluzbe.deleteById(id);
        log.info("Obrisana služba (id: {}).", id);
    }

    private Sluzba pronadjiIliBaciGresku(Long id) {
        return sluzbe.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Služba sa id " + id + " ne postoji."));
    }

}
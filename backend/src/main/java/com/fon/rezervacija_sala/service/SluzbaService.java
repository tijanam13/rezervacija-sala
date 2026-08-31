package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.SluzbaDto;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.SluzbaMapper;
import com.fon.rezervacija_sala.repository.impl.SluzbaRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SluzbaService {

    private static final Logger log = LoggerFactory.getLogger(SluzbaService.class);

    private final SluzbaRepository sluzbe;
    private final SluzbaMapper mapper;

    public SluzbaService(SluzbaRepository sluzbe, SluzbaMapper mapper) {
        this.sluzbe = sluzbe;
        this.mapper = mapper;
    }

    public List<SluzbaDto> findAll() {
        return mapper.toDtoList(sluzbe.findAll());
    }

    public SluzbaDto findById(Long id) {
        return mapper.toDto(pronadjiIliBaciGresku(id));
    }

    public SluzbaDto create(SluzbaDto dto) {
        Sluzba s = mapper.toEntity(dto);
        s.setId(null);
        sluzbe.save(s);
        log.info("Kreirana služba (id: {}, naziv: {}).", s.getId(), s.getNaziv());
        return mapper.toDto(s);
    }

    public SluzbaDto update(Long id, SluzbaDto dto) {
        Sluzba postojeca = pronadjiIliBaciGresku(id);
        postojeca.setNaziv(dto.getNaziv());
        postojeca.setOpis(dto.getOpis());
        sluzbe.save(postojeca);
        log.info("Ažurirana služba (id: {}).", id);
        return mapper.toDto(postojeca);
    }

    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id);
        sluzbe.deleteById(id);
        log.info("Obrisana služba (id: {}).", id);
    }

    private Sluzba pronadjiIliBaciGresku(Long id) {
        return sluzbe.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Služba sa id " + id + " ne postoji."));
    }

}
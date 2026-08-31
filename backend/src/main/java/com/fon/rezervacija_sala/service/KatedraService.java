package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.KatedraDto;
import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.KatedraMapper;
import com.fon.rezervacija_sala.repository.impl.KatedraRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KatedraService {

    private static final Logger log = LoggerFactory.getLogger(KatedraService.class);

    private final KatedraRepository katedre;
    private final KatedraMapper mapper;

    public KatedraService(KatedraRepository katedre, KatedraMapper mapper) {
        this.katedre = katedre;
        this.mapper = mapper;
    }

    public List<KatedraDto> findAll() {
        return mapper.toDtoList(katedre.findAll());
    }

    public KatedraDto findById(Long id) {
        return mapper.toDto(pronadjiIliBaciGresku(id));
    }

    public KatedraDto create(KatedraDto dto) {
        Katedra k = mapper.toEntity(dto);
        k.setId(null); 
        katedre.save(k);
        log.info("Kreirana katedra (id: {}, naziv: {}).", k.getId(), k.getNaziv());
        return mapper.toDto(k);
    }

    public KatedraDto update(Long id, KatedraDto dto) {
        Katedra postojeca = pronadjiIliBaciGresku(id);
        postojeca.setNaziv(dto.getNaziv());
        postojeca.setOpis(dto.getOpis());
        katedre.save(postojeca);
        log.info("Ažurirana katedra (id: {}).", id);
        return mapper.toDto(postojeca);
    }

    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id); 
        katedre.deleteById(id);
        log.info("Obrisana katedra (id: {}).", id);
    }

    private Katedra pronadjiIliBaciGresku(Long id) {
        return katedre.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Katedra sa id " + id + " ne postoji."));
    }

}
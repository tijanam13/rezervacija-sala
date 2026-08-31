package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.ZvanjeDto;
import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.ZvanjeMapper;
import com.fon.rezervacija_sala.repository.impl.ZvanjeRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ZvanjeService {

    private static final Logger log = LoggerFactory.getLogger(ZvanjeService.class);

    private final ZvanjeRepository zvanja;
    private final ZvanjeMapper mapper;

    public ZvanjeService(ZvanjeRepository zvanja, ZvanjeMapper mapper) {
        this.zvanja = zvanja;
        this.mapper = mapper;
    }

    public List<ZvanjeDto> findAll() {
        return mapper.toDtoList(zvanja.findAll());
    }

    public ZvanjeDto findById(Long id) {
        return mapper.toDto(pronadjiIliBaciGresku(id));
    }

    public ZvanjeDto create(ZvanjeDto dto) {
        Zvanje z = mapper.toEntity(dto);
        z.setId(null);
        zvanja.save(z);
        log.info("Kreirano zvanje (id: {}, naziv: {}).", z.getId(), z.getNaziv());
        return mapper.toDto(z);
    }

    public ZvanjeDto update(Long id, ZvanjeDto dto) {
        Zvanje postojece = pronadjiIliBaciGresku(id);
        postojece.setNaziv(dto.getNaziv());
        postojece.setOpis(dto.getOpis());
        zvanja.save(postojece);
        log.info("Ažurirano zvanje (id: {}).", id);
        return mapper.toDto(postojece);
    }

    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id);
        zvanja.deleteById(id);
        log.info("Obrisano zvanje (id: {}).", id);
    }

    private Zvanje pronadjiIliBaciGresku(Long id) {
        return zvanja.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Zvanje sa id " + id + " ne postoji."));
    }

}
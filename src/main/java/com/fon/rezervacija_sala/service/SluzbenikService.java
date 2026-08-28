package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.SluzbaDto;
import com.fon.rezervacija_sala.dto.SluzbenikDto;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.exception.NevalidanZahtevException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.SluzbenikMapper;
import com.fon.rezervacija_sala.repository.impl.SluzbaRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbenikRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SluzbenikService {

    private static final Logger log = LoggerFactory.getLogger(SluzbenikService.class);

    private final SluzbenikRepository sluzbenici;
    private final SluzbaRepository sluzbe;
    private final SluzbenikMapper mapper;

    public SluzbenikService(SluzbenikRepository sluzbenici, SluzbaRepository sluzbe, SluzbenikMapper mapper) {
        this.sluzbenici = sluzbenici;
        this.sluzbe = sluzbe;
        this.mapper = mapper;
    }

    public List<SluzbenikDto> findAll() {
        return mapper.toDtoList(sluzbenici.findAll());
    }

    public SluzbenikDto findById(Long id) {
        return mapper.toDto(pronadjiIliBaciGresku(id));
    }

    public List<SluzbenikDto> findBySluzba(Long sluzbaId) {
        return mapper.toDtoList(sluzbenici.findBySluzba(sluzbaId));
    }

    public SluzbenikDto update(Long id, SluzbenikDto dto) {
        Sluzbenik postojeci = pronadjiIliBaciGresku(id);
        Sluzba sluzba = pronadjiSluzbuIliBaciGresku(dto.getSluzba());

        postojeci.setIme(dto.getIme());
        postojeci.setPrezime(dto.getPrezime());
        postojeci.setBrojTelefona(dto.getBrojTelefona());
        postojeci.setBrojRadneKnjizice(dto.getBrojRadneKnjizice());
        postojeci.setPozicija(dto.getPozicija());
        postojeci.setSluzba(sluzba);

        sluzbenici.save(postojeci);
        log.info("Ažuriran profil Službenika (id: {}).", id);
        return mapper.toDto(postojeci);
    }

    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id);
        sluzbenici.deleteById(id);
        log.info("Obrisan profil Službenika (id: {}).", id);
    }

    private Sluzbenik pronadjiIliBaciGresku(Long id) {
        return sluzbenici.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Službenik sa id " + id + " ne postoji."));
    }

    private Sluzba pronadjiSluzbuIliBaciGresku(SluzbaDto sluzbaDto) {
        if (sluzbaDto == null || sluzbaDto.getId() == null) {
            throw new NevalidanZahtevException("Služba mora biti prosleđena.");
        }
        return sluzbe.findById(sluzbaDto.getId())
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Služba sa id " + sluzbaDto.getId() + " ne postoji."));
    }

}
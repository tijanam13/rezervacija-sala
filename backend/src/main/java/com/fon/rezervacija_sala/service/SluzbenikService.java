package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.SluzbaDto;
import com.fon.rezervacija_sala.dto.SluzbenikDto;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.exception.NevalidanZahtevException;
import com.fon.rezervacija_sala.exception.BrisanjeNijeMoguceException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.SluzbenikMapper;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbaRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbenikRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SluzbenikService {

    private static final Logger log = LoggerFactory.getLogger(SluzbenikService.class);

    private final SluzbenikRepository sluzbenici;
    private final SluzbaRepository sluzbe;
    private final KorisnikRepository korisnici;
    private final SluzbenikMapper mapper;

    public SluzbenikService(SluzbenikRepository sluzbenici, SluzbaRepository sluzbe,
            KorisnikRepository korisnici, SluzbenikMapper mapper) {
        this.sluzbenici = sluzbenici;
        this.sluzbe = sluzbe;
        this.korisnici = korisnici;
        this.mapper = mapper;
    }

    public List<SluzbenikDto> findAll() {
        return mapper.toDtoList(sluzbenici.findAll());
    }

    @Transactional
    public SluzbenikDto create(SluzbenikDto dto) {
        Sluzba sluzba = pronadjiSluzbuIliBaciGresku(dto.getSluzba());

        Sluzbenik s = new Sluzbenik();
        s.setIme(dto.getIme());
        s.setPrezime(dto.getPrezime());
        s.setBrojTelefona(dto.getBrojTelefona());
        s.setBrojRadneKnjizice(dto.getBrojRadneKnjizice());
        s.setPoslovniEmail(dto.getPoslovniEmail());
        s.setPozicija(dto.getPozicija());
        s.setSluzba(sluzba);

        sluzbenici.save(s);
        log.info("Kreiran novi profil Službenika (id: {}), bez naloga za prijavu.", s.getId());
        return mapper.toDto(s);
    }

    @Transactional
    public SluzbenikDto update(Long id, SluzbenikDto dto) {
        Sluzbenik postojeci = pronadjiIliBaciGresku(id);
        Sluzba sluzba = pronadjiSluzbuIliBaciGresku(dto.getSluzba());

        postojeci.setIme(dto.getIme());
        postojeci.setPrezime(dto.getPrezime());
        postojeci.setBrojTelefona(dto.getBrojTelefona());
        postojeci.setBrojRadneKnjizice(dto.getBrojRadneKnjizice());
        postojeci.setPoslovniEmail(dto.getPoslovniEmail());
        postojeci.setPozicija(dto.getPozicija());
        postojeci.setSluzba(sluzba);

        sluzbenici.save(postojeci);
        log.info("Ažuriran profil Službenika (id: {}).", id);
        return mapper.toDto(postojeci);
    }

    @Transactional
    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id);

        if (korisnici.findByZaposleniId(id).isPresent()) {
            throw new BrisanjeNijeMoguceException(
                    "Službenik (id: " + id + ") ima povezan korisnički nalog za prijavu. "
                    + "Nalog se ne može obrisati, pa se ni ovaj profil ne može obrisati. "
                    + "Nalog se može samo blokirati.");
        }

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
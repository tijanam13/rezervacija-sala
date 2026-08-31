package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.KorisnikDto;
import com.fon.rezervacija_sala.dto.StranicaDto;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.entity.Uloga;
import com.fon.rezervacija_sala.exception.NevalidanZahtevException;
import com.fon.rezervacija_sala.exception.PristupOdbijenException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.exception.StatusTranzicijaNijeDozvoljenaException;
import com.fon.rezervacija_sala.mapper.impl.KorisnikMapper;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import com.fon.rezervacija_sala.repository.impl.UlogaRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class KorisnikService {

    private static final Logger log = LoggerFactory.getLogger(KorisnikService.class);

    private final KorisnikRepository korisnici;
    private final UlogaRepository uloge;
    private final KorisnikMapper mapper;

    public KorisnikService(KorisnikRepository korisnici, UlogaRepository uloge, KorisnikMapper mapper) {
        this.korisnici = korisnici;
        this.uloge = uloge;
        this.mapper = mapper;
    }

    public StranicaDto<KorisnikDto> findAll(int stranica, int velicina, String pretraga) {
        List<KorisnikDto> sadrzaj = mapper.toDtoList(korisnici.findAllPaged(stranica, velicina, pretraga));
        long ukupno = korisnici.brojSvihKorisnika(pretraga);
        return new StranicaDto<>(sadrzaj, stranica, velicina, ukupno);
    }

    public KorisnikDto findById(Long id) {
        return mapper.toDto(pronadjiIliBaciGresku(id));
    }

    public KorisnikDto mojProfil() {
        return mapper.toDto(trenutniKorisnik());
    }

    public Korisnik trenutniKorisnik() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new PristupOdbijenException("Korisnik nije autentifikovan.");
        }

        String email = auth.getName();

        return korisnici.findByEmail(email)
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Korisnik sa email adresom " + email + " nije pronađen."));
    }

    public KorisnikDto promeniStatus(Long id, StatusNaloga noviStatus) {
        if (noviStatus == null) {
            throw new NevalidanZahtevException("Novi status naloga je obavezan.");
        }
        if (noviStatus == StatusNaloga.NEAKTIVAN) {
            throw new NevalidanZahtevException(
                    "Status NEAKTIVAN se ne postavlja ručno, već predstavlja prelazno stanje registracije.");
        }

        Korisnik k = pronadjiIliBaciGresku(id);

        if (noviStatus != StatusNaloga.AKTIVAN && k.imaUlogu(NazivUloge.ADMIN)) {
            proveriDaNijePoslednjiAdmin(k);
        }

        k.setStatus(noviStatus);
        korisnici.save(k);

        log.info("Promenjen status naloga Korisnika (id: {}) u {}.", id, noviStatus);
        return mapper.toDto(k);
    }

    public KorisnikDto dodeliUlogu(Long korisnikId, NazivUloge naziv) {
        Korisnik k = pronadjiIliBaciGresku(korisnikId);

        if (naziv == NazivUloge.KOORDINATOR && k.imaUlogu(NazivUloge.ADMIN)) {
            throw new NevalidanZahtevException(
                    "Korisnik (id: " + korisnikId + ") već ima ulogu ADMIN, koja obuhvata sve privilegije "
                    + "koordinatora - dodatna uloga KOORDINATOR nije potrebna.");
        }

        Uloga uloga = pronadjiUloguIliBaciGresku(naziv);

        if (naziv == NazivUloge.ADMIN) {
            k.getUloge().removeIf(u -> u.getNaziv() == NazivUloge.KOORDINATOR);
        }

        if (!k.imaUlogu(naziv)) {
            k.getUloge().add(uloga);
            korisnici.save(k);
            log.info("Korisniku (id: {}) dodeljena uloga {}.", korisnikId, naziv);
        } else {
            korisnici.save(k);
        }

        return mapper.toDto(k);
    }

    public KorisnikDto oduzmiUlogu(Long korisnikId, NazivUloge naziv) {
        Korisnik k = pronadjiIliBaciGresku(korisnikId);

        if (naziv == NazivUloge.ADMIN && k.imaUlogu(NazivUloge.ADMIN)) {
            proveriDaNijePoslednjiAdmin(k);
        }

        k.getUloge().removeIf(u -> u.getNaziv() == naziv);
        korisnici.save(k);

        log.info("Korisniku (id: {}) oduzeta uloga {}.", korisnikId, naziv);
        return mapper.toDto(k);
    }

    private void proveriDaNijePoslednjiAdmin(Korisnik k) {
        if (korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN) <= 1) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Korisnik (id: " + k.getId() + ") je poslednji administratorski nalog u sistemu - "
                    + "radnja nije dozvoljena.");
        }
    }

    private Korisnik pronadjiIliBaciGresku(Long id) {
        return korisnici.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Korisnik sa id " + id + " ne postoji."));
    }

    private Uloga pronadjiUloguIliBaciGresku(NazivUloge naziv) {
        return uloge.findByNaziv(naziv)
                .orElseThrow(() -> new ResursNijePronadjenException("Uloga " + naziv + " ne postoji."));
    }

}
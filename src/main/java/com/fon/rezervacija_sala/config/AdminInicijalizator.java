package com.fon.rezervacija_sala.config;

import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.entity.Uloga;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbaRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbenikRepository;
import com.fon.rezervacija_sala.repository.impl.UlogaRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInicijalizator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicijalizator.class);

    private static final String NAZIV_SISTEMSKE_SLUZBE = "Administracija sistema";

    private final KorisnikRepository korisnici;
    private final SluzbenikRepository sluzbenici;
    private final SluzbaRepository sluzbe;
    private final UlogaRepository uloge;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:admin@fon.bg.ac.rs}")
    private String adminEmail;

    @Value("${app.admin.lozinka:lozinka123}")
    private String adminLozinka;

    public AdminInicijalizator(KorisnikRepository korisnici, SluzbenikRepository sluzbenici,
            SluzbaRepository sluzbe, UlogaRepository uloge, PasswordEncoder encoder) {
        this.korisnici = korisnici;
        this.sluzbenici = sluzbenici;
        this.sluzbe = sluzbe;
        this.uloge = uloge;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        Uloga ulogaAdmin = pronadjiIliKreirajUlogu(NazivUloge.ADMIN, "Puna administrativna ovlašćenja.");

        pronadjiIliKreirajUlogu(NazivUloge.KOORDINATOR, "Odobrava/odbija rezervacije.");

        if (korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN) > 0) {
            log.info("Admin nalog već postoji.");
            return;
        }

        Sluzba sistemskaSluzba = pronadjiIliKreirajSistemskuSluzbu();

        Sluzbenik zaposleni = new Sluzbenik();
        zaposleni.setIme("Sistem");
        zaposleni.setPrezime("Administrator");
        zaposleni.setPozicija("Administrator sistema");
        zaposleni.setSluzba(sistemskaSluzba);
        sluzbenici.save(zaposleni);

        Korisnik admin = new Korisnik();
        admin.setEmail(adminEmail);
        admin.setLozinkaHash(encoder.encode(adminLozinka));
        admin.setDatumRegistracije(LocalDateTime.now());
        admin.setStatus(StatusNaloga.AKTIVAN);
        admin.setZaposleni(zaposleni);
        admin.setUloge(new HashSet<>(Set.of(ulogaAdmin)));

        korisnici.save(admin);

        log.info("Kreiran je inicijalni Admin nalog (email: {}). "
                + "Molimo promenite podrazumevanu lozinku odmah nakon prijave.", adminEmail);
    }

    private Uloga pronadjiIliKreirajUlogu(NazivUloge naziv, String opis) {
        Optional<Uloga> postojeca = uloge.findByNaziv(naziv);
        if (postojeca.isPresent()) {
            return postojeca.get();
        }
        Uloga nova = new Uloga();
        nova.setNaziv(naziv);
        nova.setOpis(opis);
        uloge.save(nova);
        return nova;
    }

    private Sluzba pronadjiIliKreirajSistemskuSluzbu() {
        Optional<Sluzba> postojeca = sluzbe.findByNaziv(NAZIV_SISTEMSKE_SLUZBE);
        if (postojeca.isPresent()) {
            return postojeca.get();
        }

        Sluzba nova = new Sluzba();
        nova.setNaziv(NAZIV_SISTEMSKE_SLUZBE);
        nova.setOpis("Automatski kreirana služba za potrebe inicijalnog administratorskog naloga.");
        sluzbe.save(nova);
        return nova;
    }

}
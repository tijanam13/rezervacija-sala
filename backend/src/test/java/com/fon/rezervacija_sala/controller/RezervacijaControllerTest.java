package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.RezervacijaSalaApplication;
import com.fon.rezervacija_sala.entity.*;
import com.fon.rezervacija_sala.repository.impl.UlogaRepository;
import com.fon.rezervacija_sala.security.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = RezervacijaSalaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class RezervacijaControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwt;

    @Autowired
    private EntityManager em;

    @Autowired
    private UlogaRepository ulogeRepo;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void ocistiTestPodatke() {
        jdbc.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbc.execute("TRUNCATE TABLE korisnik_uloga");
        jdbc.execute("TRUNCATE TABLE korisnik");
        jdbc.execute("TRUNCATE TABLE predavac");
        jdbc.execute("TRUNCATE TABLE zaposleni");
        jdbc.execute("TRUNCATE TABLE katedra");
        jdbc.execute("TRUNCATE TABLE zvanje");
        jdbc.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    private String kreirajKorisnikaIVratiToken(String email, NazivUloge... uloge) {
        return transactionTemplate.execute(status -> {
            Katedra katedra = new Katedra();
            katedra.setNaziv("Katedra za softversko inženjerstvo " + email);
            em.persist(katedra);

            Zvanje zvanje = new Zvanje(null, "Asistent " + email, null);
            em.persist(zvanje);

            Predavac predavac = new Predavac(null, "Test", "Korisnik", "0641234567",
                    "RK-" + email, null, null, katedra, zvanje);
            predavac.setPoslovniEmail(email);
            em.persist(predavac);

            Korisnik k = new Korisnik();
            k.setEmail(email);
            k.setLozinkaHash("TestLozinka123!");
            k.setDatumRegistracije(LocalDateTime.now());
            k.setStatus(StatusNaloga.AKTIVAN);
            k.setZaposleni(predavac);

            Set<Uloga> uskup = new HashSet<>();

            if (uloge != null && uloge.length > 0) {
                for (NazivUloge nu : uloge) {
                    Uloga u = ulogeRepo.findByNaziv(nu).orElseGet(() -> {
                        Uloga nova = new Uloga(null, nu, null);
                        em.persist(nova);
                        return nova;
                    });
                    uskup.add(u);
                }
            }

            k.setUloge(uskup);
            em.persist(k);
            em.flush();

            List<SimpleGrantedAuthority> authorities = uskup.stream()
                    .map(u -> new SimpleGrantedAuthority("ROLE_" + u.getNaziv().name()))
                    .toList();

            User userDetails = new User(email, "", authorities);

            List<String> rawRoles = uskup.stream().map(u -> u.getNaziv().name()).toList();
            List<String> prefixedRoles = uskup.stream().map(u -> "ROLE_" + u.getNaziv().name()).toList();

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", rawRoles);
            claims.put("roles", prefixedRoles);
            claims.put("authorities", prefixedRoles);

            return jwt.generate(userDetails, claims);
        });
    }

    private HttpHeaders zaglavljeSaTokenom(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void getSveRezervacije_bezAutentifikacije_vracaStatus401() {
        ResponseEntity<String> odgovor = restTemplate.getForEntity("/api/rezervacija", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, odgovor.getStatusCode());
    }

    @Test
    void postKreirajRezervaciju_bezAutentifikacije_vracaStatus401() {
        HttpEntity<String> zahtev = new HttpEntity<>("{}");

        ResponseEntity<String> odgovor = restTemplate.postForEntity("/api/rezervacija", zahtev, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, odgovor.getStatusCode());
    }

    @Test
    void getSveRezervacije_autentifikovanBezKoordinatorskeUloge_vracaStatus403() {
        String token = kreirajKorisnikaIVratiToken("obican.zaposleni@fon.bg.ac.rs");

        ResponseEntity<String> odgovor = restTemplate.exchange(
                "/api/rezervacija", HttpMethod.GET,
                new HttpEntity<>(zaglavljeSaTokenom(token)), String.class);

        assertEquals(HttpStatus.FORBIDDEN, odgovor.getStatusCode());
    }

    @Test
    void getSveRezervacije_autentifikovanKaoAdministrator_vracaStatus200() {
        String token = kreirajKorisnikaIVratiToken("admin.test@fon.bg.ac.rs", NazivUloge.ADMIN);

        ResponseEntity<String> odgovor = restTemplate.exchange(
                "/api/rezervacija", HttpMethod.GET,
                new HttpEntity<>(zaglavljeSaTokenom(token)), String.class);

        assertEquals(HttpStatus.OK, odgovor.getStatusCode());
    }

    @Test
    void getMojeRezervacije_autentifikovanObicanKorisnik_vracaStatus200() {
        String token = kreirajKorisnikaIVratiToken("predavac.test@fon.bg.ac.rs");

        ResponseEntity<String> odgovor = restTemplate.exchange(
                "/api/rezervacija/moje-rezervacije", HttpMethod.GET,
                new HttpEntity<>(zaglavljeSaTokenom(token)), String.class);

        assertEquals(HttpStatus.OK, odgovor.getStatusCode());
    }

    @Test
    void postKreirajRezervaciju_praznoTeloUzAutentifikaciju_vracaStatus400() {
        String token = kreirajKorisnikaIVratiToken("kreator.test@fon.bg.ac.rs");
        HttpHeaders headers = zaglavljeSaTokenom(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> odgovor = restTemplate.postForEntity(
                "/api/rezervacija", new HttpEntity<>("{}", headers), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, odgovor.getStatusCode());
    }

    @Test
    void getRezervacijaPoId_nepostojeciId_vracaStatus404() {
        String token = kreirajKorisnikaIVratiToken("citalac.test@fon.bg.ac.rs", NazivUloge.ADMIN);

        ResponseEntity<String> odgovor = restTemplate.exchange(
                "/api/rezervacija/999999", HttpMethod.GET,
                new HttpEntity<>(zaglavljeSaTokenom(token)), String.class);

        assertEquals(HttpStatus.NOT_FOUND, odgovor.getStatusCode());
    }
}
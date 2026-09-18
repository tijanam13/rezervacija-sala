package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import; 

@DataJpaTest
@Import(RezervacijaRepository.class)
@ActiveProfiles("test")
class RezervacijaRepositoryTest {

    @Autowired
    private RezervacijaRepository rezervacijaRepository;
    @Autowired
    private EntityManager em;

    private Sala sala;
    private Korisnik korisnik;
    private final LocalDate datum = LocalDate.now().plusDays(10);

    @BeforeEach
    void priprema() {
        TipSale tipSale = new TipSale(null, "Učionica", null);
        em.persist(tipSale);

        sala = new Sala();
        sala.setNaziv("Sala 1");
        sala.setZgrada("Zgrada A");
        sala.setSprat(1);
        sala.setKapacitet(20);
        sala.setBrojRacunara(0);
        sala.setStatus(StatusSale.SLOBODNA);
        sala.setTipSale(tipSale);
        em.persist(sala);

        Katedra katedra = new Katedra();
        katedra.setNaziv("Katedra 1");
        em.persist(katedra);
        Zvanje zvanje = new Zvanje(null, "Asistent", null);
        em.persist(zvanje);
        Predavac predavac = new Predavac(null, "Ime", "Prezime", "0641234567", "RK-1",
                null, null, katedra, zvanje);
        em.persist(predavac);

        korisnik = new Korisnik();
        korisnik.setEmail("test@fon.bg.ac.rs");
        korisnik.setLozinkaHash("hash");
        korisnik.setDatumRegistracije(LocalDateTime.now());
        korisnik.setStatus(StatusNaloga.AKTIVAN);
        korisnik.setZaposleni(predavac);
        em.persist(korisnik);
    }

    private Rezervacija napraviRezervaciju(LocalTime vremeOd, LocalTime vremeDo) {
        Dogadjaj svrha = new Dogadjaj(null, "Test događaj", null);
        em.persist(svrha);

        Rezervacija r = new Rezervacija();
        r.setDatumKreiranja(LocalDateTime.now());
        r.setDatumTermina(datum);
        r.setVremeOd(vremeOd);
        r.setVremeDo(vremeDo);
        r.setStatus(StatusRezervacije.NA_CEKANJU);
        r.setKorisnik(korisnik);
        r.setSvrha(svrha);
        em.persist(r);
        return r;
    }

    private StavkaRezervacije dodajStavku(Rezervacija r, int brojOsoba, StatusStavke status) {
        StavkaRezervacije st = new StavkaRezervacije();
        st.setBrojOsoba(brojOsoba);
        st.setStatusStavke(status);
        st.setSala(sala);
        r.dodajStavku(st);
        em.persist(st);
        em.flush();
        return st;
    }

    @Nested
    class ZauzetoOsobaUTerminuTest {

        @Test
        void bezPostojecihRezervacija_vracaNulu() {
            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertEquals(0, zauzeto);
        }

        @Test
        void jednaAktivnaPreklapajucaRezervacija_racunaJeUZbir() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r, 5, StatusStavke.NA_CEKANJU);

            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 30), LocalTime.of(11, 30), null);

            assertEquals(5, zauzeto);
        }

        @Test
        void otkazanaStavka_neRacunaSeUZbir() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r, 5, StatusStavke.OTKAZANA);

            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertEquals(0, zauzeto);
        }

        @Test
        void odbijenaStavka_neRacunaSeUZbir() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r, 5, StatusStavke.ODBIJENA);

            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertEquals(0, zauzeto);
        }

        @Test
        void terminKojiSeNePreklapa_neRacunaSeUZbir() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(8, 0), LocalTime.of(9, 0));
            dodajStavku(r, 5, StatusStavke.NA_CEKANJU);

            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertEquals(0, zauzeto);
        }

        @Test
        void viseAktivnihStavki_sabiraSve() {
            Rezervacija r1 = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r1, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r2 = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r2, 7, StatusStavke.ODOBRENA);

            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertEquals(12, zauzeto);
        }

        @Test
        void iskljuciStavkuId_izostavljaJuIzZbira() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            StavkaRezervacije st = dodajStavku(r, 5, StatusStavke.NA_CEKANJU);

            int zauzeto = rezervacijaRepository.zauzetoOsobaUTerminu(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), st.getId());

            assertEquals(0, zauzeto);
        }
    }

    @Nested
    class PronadjiPreklapajuceStavkeTest {

        @Test
        void bezPreklapanja_vracaPraznuListu() {
            List<StavkaRezervacije> rezultat = rezervacijaRepository.pronadjiPreklapajuceStavke(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertTrue(rezultat.isEmpty());
        }

        @Test
        void preklapajucaAktivnaStavka_pronalaziJe() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r, 5, StatusStavke.NA_CEKANJU);

            List<StavkaRezervacije> rezultat = rezervacijaRepository.pronadjiPreklapajuceStavke(
                    sala.getId(), datum, LocalTime.of(10, 30), LocalTime.of(11, 30), null);

            assertEquals(1, rezultat.size());
        }

        @Test
        void otkazanuIOdbijenuStavku_izostavljaIzRezultata() {
            Rezervacija r1 = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r1, 5, StatusStavke.OTKAZANA);
            Rezervacija r2 = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r2, 5, StatusStavke.ODBIJENA);

            List<StavkaRezervacije> rezultat = rezervacijaRepository.pronadjiPreklapajuceStavke(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), null);

            assertTrue(rezultat.isEmpty());
        }

        @Test
        void iskljuciStavkuId_izostavljaJuIzRezultata() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            StavkaRezervacije st = dodajStavku(r, 5, StatusStavke.NA_CEKANJU);

            List<StavkaRezervacije> rezultat = rezervacijaRepository.pronadjiPreklapajuceStavke(
                    sala.getId(), datum, LocalTime.of(10, 0), LocalTime.of(11, 0), st.getId());

            assertTrue(rezultat.isEmpty());
        }
    }

    @Nested
    class OstaleMetodeTest {

        @Test
        void brojSvihRezervacija_praznaBaza_vracaNulu() {
            assertEquals(0, rezervacijaRepository.brojSvihRezervacija());
        }

        @Test
        void brojSvihRezervacija_naknadnoDodataRezervacija_vracaJedan() {
            napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));

            assertEquals(1, rezervacijaRepository.brojSvihRezervacija());
        }

        @Test
        void findByStatus_filtriraPoTacnomStatusu() {
            Rezervacija r = napraviRezervaciju(LocalTime.of(10, 0), LocalTime.of(11, 0));
            dodajStavku(r, 5, StatusStavke.NA_CEKANJU);

            List<Rezervacija> naCekanju = rezervacijaRepository.findByStatus(StatusRezervacije.NA_CEKANJU);
            List<Rezervacija> odobrene = rezervacijaRepository.findByStatus(StatusRezervacije.ODOBRENA);

            assertEquals(1, naCekanju.size());
            assertTrue(odobrene.isEmpty());
        }
    }
}
package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.KorisnikDto;
import com.fon.rezervacija_sala.dto.StranicaDto;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.entity.Uloga;
import com.fon.rezervacija_sala.exception.*;
import com.fon.rezervacija_sala.mapper.impl.KorisnikMapper;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import com.fon.rezervacija_sala.repository.impl.UlogaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KorisnikServiceTest {

    @Mock
    private KorisnikRepository korisnici;
    @Mock
    private UlogaRepository uloge;
    @Mock
    private KorisnikMapper mapper;

    private KorisnikService servis;

    @BeforeEach
    void priprema() {
        servis = new KorisnikService(korisnici, uloge, mapper);
    }

    @AfterEach
    void ciscenje() {
        SecurityContextHolder.clearContext();
    }

    private void prijaviKao(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Korisnik napraviKorisnika(Long id, NazivUloge... uloge) {
        Korisnik k = new Korisnik(id);
        k.setEmail("korisnik" + id + "@fon.bg.ac.rs");
        Set<Uloga> skup = new HashSet<>();
        for (NazivUloge nu : uloge) {
            skup.add(new Uloga(null, nu, null));
        }
        k.setUloge(skup);
        return k;
    }

    @Nested
    class FindAllTest {

        @Test
        void findAll_vracaFiltriranuStranicu() {
            when(korisnici.findAllPaged(0, 10, "tijana")).thenReturn(List.of(new Korisnik(1L)));
            when(korisnici.brojSvihKorisnika("tijana")).thenReturn(1L);
            when(mapper.toDtoList(any())).thenReturn(List.of(new KorisnikDto()));

            StranicaDto<KorisnikDto> rezultat = servis.findAll(0, 10, "tijana");

            assertEquals(1L, rezultat.getUkupnoElemenata());
        }
    }

    @Nested
    class MojProfilTest {

        @Test
        void mojProfil_prijavljenKorisnik_vracaProfil() {
            Korisnik k = napraviKorisnika(1L);
            prijaviKao(k.getEmail());
            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            assertNotNull(servis.mojProfil());
        }

        @Test
        void mojProfil_neprijavljenKorisnik_bacaIzuzetak() {
            PristupOdbijenException izuzetak = assertThrows(PristupOdbijenException.class,
                    () -> servis.mojProfil());
            assertEquals("Korisnik nije autentifikovan.", izuzetak.getMessage());
        }
    }

    @Nested
    class TrenutniKorisnikTest {

        @Test
        void trenutniKorisnik_neautentifikovan_bacaIzuzetak() {
            PristupOdbijenException izuzetak = assertThrows(PristupOdbijenException.class,
                    () -> servis.trenutniKorisnik());
            assertEquals("Korisnik nije autentifikovan.", izuzetak.getMessage());
        }

        @Test
        void trenutniKorisnik_autentifikovanAliNijePronadjenUBazi_bacaIzuzetak() {
            prijaviKao("nepostojeci@fon.bg.ac.rs");
            when(korisnici.findByEmail("nepostojeci@fon.bg.ac.rs")).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.trenutniKorisnik());
            assertTrue(izuzetak.getMessage().contains("nije pronađen"));
        }

        @Test
        void trenutniKorisnik_autentifikovanIPronadjen_vracaGa() {
            Korisnik k = napraviKorisnika(1L);
            prijaviKao(k.getEmail());
            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            assertEquals(k, servis.trenutniKorisnik());
        }
    }

    @Nested
    class PromeniStatusTest {

        @Test
        void promeniStatus_noviStatusNull_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.promeniStatus(1L, null));
            assertEquals("Novi status naloga je obavezan.", izuzetak.getMessage());
        }

        @Test
        void promeniStatus_pokusajNeaktivan_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.promeniStatus(1L, StatusNaloga.NEAKTIVAN));
            assertTrue(izuzetak.getMessage().contains("NEAKTIVAN se ne postavlja ručno"));
        }

        @Test
        void promeniStatus_korisnikNePostoji_bacaIzuzetak() {
            when(korisnici.findById(99L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.promeniStatus(99L, StatusNaloga.BLOKIRAN));
            assertEquals("Korisnik ne postoji.", izuzetak.getMessage());
        }

        @Test
        void promeniStatus_blokiranjeSopstvenogNaloga_bacaIzuzetak() {
            Korisnik k = napraviKorisnika(1L);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            prijaviKao(k.getEmail());
            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.promeniStatus(1L, StatusNaloga.BLOKIRAN));
            assertEquals("Ne možete blokirati svoj nalog.", izuzetak.getMessage());
        }

        @Test
        void promeniStatus_blokiranjePoslednjegAdmina_bacaIzuzetak() {
            Korisnik meta = napraviKorisnika(2L, NazivUloge.ADMIN);
            Korisnik drugiAdmin = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(2L)).thenReturn(Optional.of(meta));
            prijaviKao(drugiAdmin.getEmail());
            when(korisnici.findByEmail(drugiAdmin.getEmail())).thenReturn(Optional.of(drugiAdmin));
            when(korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN)).thenReturn(1L);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.promeniStatus(2L, StatusNaloga.BLOKIRAN));
            assertTrue(izuzetak.getMessage().contains("poslednji administratorski nalog"));
        }

        @Test
        void promeniStatus_blokiranjeAdminaKadIhImaVise_uspeva() {
            Korisnik meta = napraviKorisnika(2L, NazivUloge.ADMIN);
            Korisnik drugiAdmin = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(2L)).thenReturn(Optional.of(meta));
            prijaviKao(drugiAdmin.getEmail());
            when(korisnici.findByEmail(drugiAdmin.getEmail())).thenReturn(Optional.of(drugiAdmin));
            when(korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN)).thenReturn(2L);
            when(mapper.toDto(meta)).thenReturn(new KorisnikDto());

            assertNotNull(servis.promeniStatus(2L, StatusNaloga.BLOKIRAN));
            assertEquals(StatusNaloga.BLOKIRAN, meta.getStatus());
        }

        @Test
        void promeniStatus_obicanKorisnik_uspeva() {
            Korisnik k = napraviKorisnika(1L);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            assertNotNull(servis.promeniStatus(1L, StatusNaloga.AKTIVAN));
            assertEquals(StatusNaloga.AKTIVAN, k.getStatus());
        }
    }

    @Nested
    class DodeliUloguTest {

        @Test
        void dodeliUlogu_korisnikNePostoji_bacaIzuzetak() {
            when(korisnici.findById(99L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.dodeliUlogu(99L, NazivUloge.KOORDINATOR));
            assertEquals("Korisnik ne postoji.", izuzetak.getMessage());
        }

        @Test
        void dodeliUlogu_ulogaNePostoji_bacaIzuzetak() {
            Korisnik k = napraviKorisnika(1L);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(uloge.findByNaziv(NazivUloge.KOORDINATOR)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.dodeliUlogu(1L, NazivUloge.KOORDINATOR));
            assertTrue(izuzetak.getMessage().contains("ne postoji"));
        }

        @Test
        void dodeliUlogu_dodelaAdminUklanjaKoordinatorUlogu() {
            Korisnik k = napraviKorisnika(1L, NazivUloge.KOORDINATOR);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(uloge.findByNaziv(NazivUloge.ADMIN)).thenReturn(Optional.of(new Uloga(1L, NazivUloge.ADMIN, null)));
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            servis.dodeliUlogu(1L, NazivUloge.ADMIN);

            assertTrue(k.imaUlogu(NazivUloge.ADMIN));
            assertFalse(k.imaUlogu(NazivUloge.KOORDINATOR));
        }

        @Test
        void dodeliUlogu_koordinatorPoslednjemAdminu_bacaIzuzetak() {
            Korisnik k = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(uloge.findByNaziv(NazivUloge.KOORDINATOR))
                    .thenReturn(Optional.of(new Uloga(2L, NazivUloge.KOORDINATOR, null)));
            when(korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN)).thenReturn(1L);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.dodeliUlogu(1L, NazivUloge.KOORDINATOR));
            assertTrue(izuzetak.getMessage().contains("poslednji administratorski nalog"));
        }

        @Test
        void dodeliUlogu_koordinatorAdminuKadIhImaVise_uklanjaAdminDodajeKoordinatora() {
            Korisnik k = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(uloge.findByNaziv(NazivUloge.KOORDINATOR))
                    .thenReturn(Optional.of(new Uloga(2L, NazivUloge.KOORDINATOR, null)));
            when(korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN)).thenReturn(2L);
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            servis.dodeliUlogu(1L, NazivUloge.KOORDINATOR);

            assertTrue(k.imaUlogu(NazivUloge.KOORDINATOR));
            assertFalse(k.imaUlogu(NazivUloge.ADMIN));
        }

        @Test
        void dodeliUlogu_vecImaUlogu_neduplira() {
            Korisnik k = napraviKorisnika(1L, NazivUloge.KOORDINATOR);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(uloge.findByNaziv(NazivUloge.KOORDINATOR))
                    .thenReturn(Optional.of(new Uloga(2L, NazivUloge.KOORDINATOR, null)));
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            servis.dodeliUlogu(1L, NazivUloge.KOORDINATOR);

            assertEquals(1, k.getUloge().size());
        }

        @Test
        void dodeliUlogu_novaUlogaBezSukoba_uspesnoDodaje() {
            Korisnik k = napraviKorisnika(1L);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(uloge.findByNaziv(NazivUloge.KOORDINATOR))
                    .thenReturn(Optional.of(new Uloga(2L, NazivUloge.KOORDINATOR, null)));
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            servis.dodeliUlogu(1L, NazivUloge.KOORDINATOR);

            assertTrue(k.imaUlogu(NazivUloge.KOORDINATOR));
        }
    }

    @Nested
    class OduzmiUloguTest {

        @Test
        void oduzmiUlogu_korisnikNePostoji_bacaIzuzetak() {
            when(korisnici.findById(99L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.oduzmiUlogu(99L, NazivUloge.KOORDINATOR));
            assertEquals("Korisnik ne postoji.", izuzetak.getMessage());
        }

        @Test
        void oduzmiUlogu_sebiAdminUlogu_bacaIzuzetak() {
            Korisnik k = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            prijaviKao(k.getEmail());
            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.oduzmiUlogu(1L, NazivUloge.ADMIN));
            assertEquals("Ne možete sami sebi oduzeti ulogu ADMIN.", izuzetak.getMessage());
        }

        @Test
        void oduzmiUlogu_poslednjemAdminu_bacaIzuzetak() {
            Korisnik meta = napraviKorisnika(2L, NazivUloge.ADMIN);
            Korisnik drugiAdmin = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(2L)).thenReturn(Optional.of(meta));
            prijaviKao(drugiAdmin.getEmail());
            when(korisnici.findByEmail(drugiAdmin.getEmail())).thenReturn(Optional.of(drugiAdmin));
            when(korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN)).thenReturn(1L);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.oduzmiUlogu(2L, NazivUloge.ADMIN));
            assertTrue(izuzetak.getMessage().contains("poslednji administratorski nalog"));
        }

        @Test
        void oduzmiUlogu_adminKadIhImaVise_uspesnoOduzima() {
            Korisnik meta = napraviKorisnika(2L, NazivUloge.ADMIN);
            Korisnik drugiAdmin = napraviKorisnika(1L, NazivUloge.ADMIN);
            when(korisnici.findById(2L)).thenReturn(Optional.of(meta));
            prijaviKao(drugiAdmin.getEmail());
            when(korisnici.findByEmail(drugiAdmin.getEmail())).thenReturn(Optional.of(drugiAdmin));
            when(korisnici.brojKorisnikaSaUlogom(NazivUloge.ADMIN)).thenReturn(2L);
            when(mapper.toDto(meta)).thenReturn(new KorisnikDto());

            servis.oduzmiUlogu(2L, NazivUloge.ADMIN);

            assertFalse(meta.imaUlogu(NazivUloge.ADMIN));
        }

        @Test
        void oduzmiUlogu_neAdminUlogu_uspesnoOduzimaBezDodatnihProvera() {
            Korisnik k = napraviKorisnika(1L, NazivUloge.KOORDINATOR);
            when(korisnici.findById(1L)).thenReturn(Optional.of(k));
            when(mapper.toDto(k)).thenReturn(new KorisnikDto());

            servis.oduzmiUlogu(1L, NazivUloge.KOORDINATOR);

            assertFalse(k.imaUlogu(NazivUloge.KOORDINATOR));
        }
    }
}
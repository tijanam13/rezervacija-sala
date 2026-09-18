package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.*;
import com.fon.rezervacija_sala.entity.*;
import com.fon.rezervacija_sala.exception.*;
import com.fon.rezervacija_sala.mapper.impl.KorisnikMapper;
import com.fon.rezervacija_sala.repository.impl.*;
import com.fon.rezervacija_sala.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private JwtService jwt;
    @Mock
    private KorisnikRepository korisnici;
    @Mock
    private PredavacRepository predavci;
    @Mock
    private SluzbenikRepository sluzbenici;
    @Mock
    private VerifikacioniTokenRepository verifikacioniTokeni;
    @Mock
    private ResetLozinkeTokenRepository resetTokeni;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private MailService mail;
    @Mock
    private KorisnikMapper korisnikMapper;

    private AuthService servis;

    @BeforeEach
    void priprema() {
        servis = new AuthService(authManager, jwt, korisnici, predavci, sluzbenici,
                verifikacioniTokeni, resetTokeni, encoder, mail, korisnikMapper);

        ReflectionTestUtils.setField(servis, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(servis, "maxNeuspesnihPokusaja", 5);
        ReflectionTestUtils.setField(servis, "trajanjeZakljucavanjaMinuti", 15L);
    }

    @Nested
    class RegisterKorisnikTest {

        @Test
        void register_nePostojiProfilZaposlenog_bacaIzuzetak() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("nepostojeci@fon.bg.ac.rs");
            req.setLozinka("Lozinka123!");

            when(predavci.findByPoslovniEmail(req.getEmail())).thenReturn(Optional.empty());
            when(sluzbenici.findByPoslovniEmail(req.getEmail())).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.registerKorisnik(req));
            assertTrue(izuzetak.getMessage().contains("Ne postoji profil predavača"));
        }

        @Test
        void register_profilVecImaNalog_bacaIzuzetak() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("tijana.milosavljevic@fon.bg.ac.rs");
            req.setLozinka("Lozinka123!");

            Predavac profil = new Predavac(1L, "Tijana", "Milosavljević", "0641234567", "RK-1", null, null, null, null);
            when(predavci.findByPoslovniEmail(req.getEmail())).thenReturn(Optional.of(profil));
            when(korisnici.findByZaposleniId(1L)).thenReturn(Optional.of(new Korisnik(5L)));

            EmailZauzetException izuzetak = assertThrows(EmailZauzetException.class,
                    () -> servis.registerKorisnik(req));
            assertEquals("Email adresa je već u upotrebi.", izuzetak.getMessage());
        }

        @Test
        void register_ispravanZahtev_uspesnoRegistrujeISaljeMejl() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("tijana.milosavljevic@fon.bg.ac.rs");
            req.setLozinka("Lozinka123!");

            Predavac profil = new Predavac(1L, "Tijana", "Milosavljević", "0641234567", "RK-1", null, null, null, null);
            when(predavci.findByPoslovniEmail(req.getEmail())).thenReturn(Optional.of(profil));
            when(korisnici.findByZaposleniId(1L)).thenReturn(Optional.empty());
            when(encoder.encode(req.getLozinka())).thenReturn("hash");
            when(korisnikMapper.toDto(any())).thenReturn(new KorisnikDto());

            assertNotNull(servis.registerKorisnik(req));

            verify(korisnici, times(1)).save(argThat(k -> k.getStatus() == StatusNaloga.NEAKTIVAN));
            verify(mail, times(1)).sendHtml(eq(req.getEmail()), any(), any());
        }
    }

    @Nested
    class VerifyEmailTest {

        @Test
        void verifyEmail_nepostojeciToken_bacaIzuzetak() {
            when(verifikacioniTokeni.find("nevazeci")).thenReturn(null);

            TokenNevalidanException izuzetak = assertThrows(TokenNevalidanException.class,
                    () -> servis.verifyEmail("nevazeci"));
            assertEquals("Neispravan token.", izuzetak.getMessage());
        }

        @Test
        void verifyEmail_istekaoToken_brisegaIBacaIzuzetak() {
            Korisnik k = new Korisnik(1L);
            VerifikacioniToken vt = mock(VerifikacioniToken.class);
            when(vt.isIstekao()).thenReturn(true);
            when(verifikacioniTokeni.find("istekli-token")).thenReturn(vt);

            TokenNevalidanException izuzetak = assertThrows(TokenNevalidanException.class,
                    () -> servis.verifyEmail("istekli-token"));

            assertEquals("Token je istekao.", izuzetak.getMessage());
            verify(verifikacioniTokeni, times(1)).delete(vt);
        }

        @Test
        void verifyEmail_ispravanToken_aktivirajNalog() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setStatus(StatusNaloga.NEAKTIVAN);

            VerifikacioniToken vt = mock(VerifikacioniToken.class);
            when(vt.isIstekao()).thenReturn(false);
            when(vt.getKorisnik()).thenReturn(k);
            when(verifikacioniTokeni.find("ispravan-token")).thenReturn(vt);
            when(jwt.generate(any(), any())).thenReturn("jwt-token");
            when(korisnikMapper.toDto(any())).thenReturn(new KorisnikDto());

            AuthResponse odgovor = servis.verifyEmail("ispravan-token");

            assertNotNull(odgovor);
            assertEquals(StatusNaloga.AKTIVAN, k.getStatus());
            verify(verifikacioniTokeni, times(1)).delete(vt);
        }
    }

    @Nested
    class LoginTest {

        @Test
        void login_vecZakljucanNalog_bacaIzuzetak() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setZakljucanDo(LocalDateTime.now().plusMinutes(10));
            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            LoginRequest req = new LoginRequest();
            req.setEmail(k.getEmail());
            req.setLozinka("bilo šta");

            NalogZakljucanException izuzetak = assertThrows(NalogZakljucanException.class,
                    () -> servis.login(req));
            assertTrue(izuzetak.getMessage().contains("privremeno zaključan"));
            verifyNoInteractions(authManager);
        }

        @Test
        void login_petiUzastopniNeuspesniPokusaj_zakljucavaNalog() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setBrojNeuspesnihPokusaja(4);

            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));
            when(korisnici.findByEmailForUpdate(k.getEmail())).thenReturn(Optional.of(k));
            when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Pogrešna lozinka"));

            LoginRequest req = new LoginRequest();
            req.setEmail(k.getEmail());
            req.setLozinka("pogresna");

            BadCredentialsException izuzetak = assertThrows(BadCredentialsException.class, () -> servis.login(req));
            assertEquals("Pogrešna lozinka", izuzetak.getMessage());

            assertEquals(5, k.getBrojNeuspesnihPokusaja());
            assertNotNull(k.getZakljucanDo());
        }

        @ParameterizedTest(name = "početni broj pokušaja={0}, nakon novog neuspešnog treba biti zaključan={1}")
        @CsvSource({
            "0, false",
            "1, false",
            "2, false",
            "3, false",
            "4, true"
        })
        void login_granicaZakljucavanja(int pocetniBroj, boolean trebaBitiZakljucan) {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setBrojNeuspesnihPokusaja(pocetniBroj);

            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));
            when(korisnici.findByEmailForUpdate(k.getEmail())).thenReturn(Optional.of(k));
            when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Pogrešna lozinka"));

            LoginRequest req = new LoginRequest();
            req.setEmail(k.getEmail());
            req.setLozinka("pogresna");

            BadCredentialsException izuzetak = assertThrows(BadCredentialsException.class, () -> servis.login(req));
            assertEquals("Pogrešna lozinka", izuzetak.getMessage());

            assertEquals(pocetniBroj + 1, k.getBrojNeuspesnihPokusaja());
            if (trebaBitiZakljucan) {
                assertNotNull(k.getZakljucanDo());
            } else {
                assertNull(k.getZakljucanDo());
            }
        }

        @Test
        void login_neaktivanNalog_bacaIzuzetak() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setStatus(StatusNaloga.NEAKTIVAN);

            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            LoginRequest req = new LoginRequest();
            req.setEmail(k.getEmail());
            req.setLozinka("ispravnaLozinka");

            NalogNijeAktivanException izuzetak = assertThrows(NalogNijeAktivanException.class,
                    () -> servis.login(req));
            assertTrue(izuzetak.getMessage().contains("nije aktiviran"));
        }

        @Test
        void login_blokiranNalog_bacaIzuzetak() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setStatus(StatusNaloga.BLOKIRAN);

            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            LoginRequest req = new LoginRequest();
            req.setEmail(k.getEmail());
            req.setLozinka("ispravnaLozinka");

            NalogNijeAktivanException izuzetak = assertThrows(NalogNijeAktivanException.class,
                    () -> servis.login(req));
            assertTrue(izuzetak.getMessage().contains("blokiran"));
        }

        @Test
        void login_uspesnaPrijava_resetujeBrojacIVracaOdgovor() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");
            k.setStatus(StatusNaloga.AKTIVAN);
            k.setBrojNeuspesnihPokusaja(2);
            k.setUloge(Set.of());

            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));
            when(jwt.generate(any(), any())).thenReturn("jwt-token");
            when(korisnikMapper.toDto(any())).thenReturn(new KorisnikDto());

            LoginRequest req = new LoginRequest();
            req.setEmail(k.getEmail());
            req.setLozinka("ispravnaLozinka");

            AuthResponse odgovor = servis.login(req);

            assertNotNull(odgovor);
            assertEquals(0, k.getBrojNeuspesnihPokusaja());
        }
    }

    @Nested
    class RequestPasswordResetTest {

        @Test
        void requestReset_nepostojeciEmail_nistaNeRadi() {
            when(korisnici.findByEmail("nepostojeci@fon.bg.ac.rs")).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> servis.requestPasswordReset("nepostojeci@fon.bg.ac.rs"));

            verifyNoInteractions(mail);
            verify(resetTokeni, never()).save(any());
        }

        @Test
        void requestReset_postojeciEmail_saljeMejl() {
            Predavac profil = new Predavac(1L, "Tijana", "Milosavljević", "0641234567", "RK-1", null, null, null, null);
            Korisnik k = new Korisnik(1L);
            k.setEmail("tijana.milosavljevic@fon.bg.ac.rs");
            k.setZaposleni(profil);

            when(korisnici.findByEmail(k.getEmail())).thenReturn(Optional.of(k));

            servis.requestPasswordReset(k.getEmail());

            verify(resetTokeni, times(1)).save(any());
            verify(mail, times(1)).sendHtml(eq(k.getEmail()), any(), any());
        }
    }

    @Nested
    class ResetPasswordTest {

        @Test
        void resetPassword_nepostojeciToken_bacaIzuzetak() {
            when(resetTokeni.find("nevazeci")).thenReturn(null);

            TokenNevalidanException izuzetak = assertThrows(TokenNevalidanException.class,
                    () -> servis.resetPassword("nevazeci", "NovaLozinka123!"));
            assertEquals("Token je neispravan ili je istekao.", izuzetak.getMessage());
        }

        @Test
        void resetPassword_vecKoriscenToken_bacaIzuzetak() {
            ResetLozinkeToken t = mock(ResetLozinkeToken.class);
            when(t.isKoriscen()).thenReturn(true);
            when(resetTokeni.find("koriscen-token")).thenReturn(t);

            TokenNevalidanException izuzetak = assertThrows(TokenNevalidanException.class,
                    () -> servis.resetPassword("koriscen-token", "NovaLozinka123!"));
            assertEquals("Token je neispravan ili je istekao.", izuzetak.getMessage());
        }

        @Test
        void resetPassword_istekaoToken_bacaIzuzetak() {
            ResetLozinkeToken t = mock(ResetLozinkeToken.class);
            when(t.isKoriscen()).thenReturn(false);
            when(t.isIstekao()).thenReturn(true);
            when(resetTokeni.find("istekli-token")).thenReturn(t);

            TokenNevalidanException izuzetak = assertThrows(TokenNevalidanException.class,
                    () -> servis.resetPassword("istekli-token", "NovaLozinka123!"));
            assertEquals("Token je neispravan ili je istekao.", izuzetak.getMessage());
        }

        @Test
        void resetPassword_ispravanToken_uspesnoMenjaLozinku() {
            Korisnik k = new Korisnik(1L);
            k.setEmail("test@fon.bg.ac.rs");

            ResetLozinkeToken t = mock(ResetLozinkeToken.class);
            when(t.isKoriscen()).thenReturn(false);
            when(t.isIstekao()).thenReturn(false);
            when(t.getKorisnik()).thenReturn(k);
            when(resetTokeni.find("ispravan-token")).thenReturn(t);
            when(encoder.encode("NovaLozinka123!")).thenReturn("novi-hash");

            servis.resetPassword("ispravan-token", "NovaLozinka123!");

            assertEquals("novi-hash", k.getLozinkaHash());
            verify(t, times(1)).setKoriscen(true);
            verify(korisnici, times(1)).save(k);
            verify(resetTokeni, times(1)).save(t);
        }
    }
}
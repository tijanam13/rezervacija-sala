package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.AuthResponse;
import com.fon.rezervacija_sala.dto.KorisnikDto;
import com.fon.rezervacija_sala.dto.LoginRequest;
import com.fon.rezervacija_sala.dto.RegisterPredavacRequest;
import com.fon.rezervacija_sala.dto.RegisterSluzbenikRequest;
import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.entity.ResetLozinkeToken;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.entity.VerifikacioniToken;
import com.fon.rezervacija_sala.entity.Zaposleni;
import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.exception.EmailZauzetException;
import com.fon.rezervacija_sala.exception.NalogNijeAktivanException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.exception.TokenNevalidanException;
import com.fon.rezervacija_sala.mapper.impl.KorisnikMapper;
import com.fon.rezervacija_sala.repository.impl.KorisnikRepository;
import com.fon.rezervacija_sala.repository.impl.PredavacRepository;
import com.fon.rezervacija_sala.repository.impl.ResetLozinkeTokenRepository;
import com.fon.rezervacija_sala.repository.impl.SluzbenikRepository;
import com.fon.rezervacija_sala.repository.impl.VerifikacioniTokenRepository;
import com.fon.rezervacija_sala.security.JwtService;
import jakarta.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final KorisnikRepository korisnici;
    private final PredavacRepository predavci;
    private final SluzbenikRepository sluzbenici;
    private final VerifikacioniTokenRepository verifikacioniTokeni;
    private final ResetLozinkeTokenRepository resetTokeni;
    private final PasswordEncoder encoder;
    private final MailService mail;
    private final KorisnikMapper korisnikMapper;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthService(AuthenticationManager authManager, JwtService jwt, KorisnikRepository korisnici,
            PredavacRepository predavci, SluzbenikRepository sluzbenici,
            VerifikacioniTokenRepository verifikacioniTokeni, ResetLozinkeTokenRepository resetTokeni,
            PasswordEncoder encoder, MailService mail, KorisnikMapper korisnikMapper) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.korisnici = korisnici;
        this.predavci = predavci;
        this.sluzbenici = sluzbenici;
        this.verifikacioniTokeni = verifikacioniTokeni;
        this.resetTokeni = resetTokeni;
        this.encoder = encoder;
        this.mail = mail;
        this.korisnikMapper = korisnikMapper;
    }

    @Transactional
    public KorisnikDto registerPredavac(RegisterPredavacRequest req) {
        proveriDaEmailNijeZauzet(req.getEmail());

        Predavac zaposleni = new Predavac();
        zaposleni.setIme(req.getIme());
        zaposleni.setPrezime(req.getPrezime());
        zaposleni.setBrojTelefona(req.getBrojTelefona());
        zaposleni.setBrojRadneKnjizice(req.getBrojRadneKnjizice());
        zaposleni.setTitula(req.getTitula());
        zaposleni.setTerminKonsultacija(req.getTerminKonsultacija());
        zaposleni.setKatedra(new Katedra(req.getKatedraId()));
        zaposleni.setZvanje(new Zvanje(req.getZvanjeId()));
        predavci.save(zaposleni);

        Korisnik k = novKorisnikNalog(req.getEmail(), req.getLozinka(), zaposleni);

        posaljiVerifikacioniEmail(k);
        log.info("Registrovan novi Predavač nalog (email: {}), čeka verifikaciju email-a.", k.getEmail());

        return korisnikMapper.toDto(k);
    }

    @Transactional
    public KorisnikDto registerSluzbenik(RegisterSluzbenikRequest req) {
        proveriDaEmailNijeZauzet(req.getEmail());

        Sluzbenik zaposleni = new Sluzbenik();
        zaposleni.setIme(req.getIme());
        zaposleni.setPrezime(req.getPrezime());
        zaposleni.setBrojTelefona(req.getBrojTelefona());
        zaposleni.setBrojRadneKnjizice(req.getBrojRadneKnjizice());
        zaposleni.setPozicija(req.getPozicija());
        zaposleni.setSluzba(new Sluzba(req.getSluzbaId()));
        sluzbenici.save(zaposleni);

        Korisnik k = novKorisnikNalog(req.getEmail(), req.getLozinka(), zaposleni);

        posaljiVerifikacioniEmail(k);
        log.info("Registrovan novi Službenik nalog (email: {}), čeka verifikaciju email-a.", k.getEmail());

        return korisnikMapper.toDto(k);
    }

    private Korisnik novKorisnikNalog(String email, String lozinka, Zaposleni zaposleni) {
        Korisnik k = new Korisnik();
        k.setEmail(email);
        k.setLozinkaHash(encoder.encode(lozinka));
        k.setDatumRegistracije(LocalDateTime.now());
        k.setStatus(StatusNaloga.NEAKTIVAN); 
        k.setZaposleni(zaposleni);
        korisnici.save(k);
        return k;
    }

    private void proveriDaEmailNijeZauzet(String email) {
        if (korisnici.findByEmail(email).isPresent()) {
            throw new EmailZauzetException("Email adresa je već u upotrebi.");
        }
    }

    private void posaljiVerifikacioniEmail(Korisnik k) {
        VerifikacioniToken vt = VerifikacioniToken.of(k, 86400); 
        verifikacioniTokeni.save(vt);

        String verifyUrl = frontendUrl + "/verifikuj?token=" + URLEncoder.encode(vt.getToken(), StandardCharsets.UTF_8);
        String html = buildVerifikacioniEmailHtml(k.getZaposleni().getIme(), verifyUrl);
        mail.sendHtml(k.getEmail(), "Potvrda naloga - Rezervacija sala", html);
    }

    private String buildVerifikacioniEmailHtml(String ime, String link) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px;">
          <div style="background:#ffffff; border:1px solid #ccc; border-radius:14px; padding:24px;">
            <h2 style="margin:0 0 8px;">Pozdrav %s,</h2>
            <p style="margin:0 0 16px;">Hvala na registraciji na sistem za rezervaciju sala. Kliknite na dugme ispod da potvrdite svoj nalog.</p>
            <div style="text-align:center; margin:24px 0;">
              <a href="%s" style="display:inline-block; padding:12px 18px; background:#3f51b5; color:#ffffff; text-decoration:none; border-radius:10px; font-weight:700;">
                Potvrdi nalog
              </a>
            </div>
            <p style="margin:0 0 6px; font-size:14px;">Ako dugme ne radi, otvorite sledeći link u pregledaču:</p>
            <p style="margin:0; word-break:break-all; font-size:13px;">%s</p>
            <hr style="border:none; border-top:1px solid #ccc; margin:20px 0;">
            <p style="margin:0; font-size:12px;">Link važi 24 sata.</p>
          </div>
        </div>
        """.formatted(ime, link, link);
    }

    @Transactional
    public AuthResponse verifyEmail(String token) {
        VerifikacioniToken vt = verifikacioniTokeni.find(token);
        if (vt == null) {
            throw new TokenNevalidanException("Neispravan token.");
        }
        if (vt.isIstekao()) {
            verifikacioniTokeni.delete(vt);
            throw new TokenNevalidanException("Token je istekao.");
        }

        Korisnik k = vt.getKorisnik();
        verifikacioniTokeni.delete(vt);

        k.setStatus(StatusNaloga.AKTIVAN);
        korisnici.save(k);

        log.info("Email verifikovan (email: {}), nalog je AKTIVAN.", k.getEmail());
        return izgradiAuthResponse(k);
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getLozinka())
        );

        Korisnik k = korisnici.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResursNijePronadjenException("Korisnik nije pronađen."));

        proveriStatusNaloga(k.getStatus());
        return izgradiAuthResponse(k);
    }

    private void proveriStatusNaloga(StatusNaloga status) {
        switch (status) {
            case NEAKTIVAN -> {
                log.warn("Pokušaj prijave na neaktiviran nalog (status: {}).", status);
                throw new NalogNijeAktivanException("Nalog nije aktiviran, proverite email za verifikacioni link.");
            }
            case BLOKIRAN -> {
                log.warn("Pokušaj prijave na blokiran nalog (status: {}).", status);
                throw new NalogNijeAktivanException("Nalog je blokiran. Obratite se administratoru.");
            }
            case AKTIVAN -> {
            }
        }
    }

    private AuthResponse izgradiAuthResponse(Korisnik k) {
        List<String> uloge = k.getUloge().stream()
                .map(u -> "ROLE_" + u.getNaziv().name())
                .toList();

        List<SimpleGrantedAuthority> authorities = uloge.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        User userDetails = new User(k.getEmail(), "", authorities);

        List<String> roleClaims = uloge.stream().map(r -> r.replace("ROLE_", "")).toList();
        String token = jwt.generate(userDetails, Map.of("role", roleClaims));

        return new AuthResponse(token, korisnikMapper.toDto(k), uloge);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Korisnik k = korisnici.findByEmail(email).orElse(null);
        if (k == null) {
            return; 
        }

        ResetLozinkeToken t = ResetLozinkeToken.of(k, 1800); 
        resetTokeni.save(t);

        String link = frontendUrl + "/reset-lozinke?token=" + URLEncoder.encode(t.getToken(), StandardCharsets.UTF_8);
        String html = buildResetEmailHtml(k.getZaposleni().getIme(), link);
        mail.sendHtml(k.getEmail(), "Reset lozinke - Rezervacija sala", html);
    }

    private String buildResetEmailHtml(String ime, String link) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px;">
          <div style="background:#ffffff; border:1px solid #ccc; border-radius:14px; padding:24px;">
            <h2 style="margin:0 0 8px;">Pozdrav %s,</h2>
            <p style="margin:0 0 16px;">Poslali ste zahtev za reset lozinke. Kliknite na dugme ispod da postavite novu lozinku.</p>
            <div style="text-align:center; margin:24px 0;">
              <a href="%s" style="display:inline-block; padding:12px 18px; background:#3f51b5; color:#ffffff; text-decoration:none; border-radius:10px; font-weight:700;">
                Postavi novu lozinku
              </a>
            </div>
            <p style="margin:0 0 6px; font-size:14px;">Ako dugme ne radi, otvorite sledeći link u pregledaču:</p>
            <p style="margin:0; word-break:break-all; font-size:13px;">%s</p>
            <hr style="border:none; border-top:1px solid #ccc; margin:20px 0;">
            <p style="margin:0; font-size:12px;">Link važi 30 minuta.</p>
          </div>
        </div>
        """.formatted(ime, link, link);
    }

    @Transactional
    public void resetPassword(String token, String novaLozinka) {
        ResetLozinkeToken t = resetTokeni.find(token);
        if (t == null || t.isKoriscen() || t.isIstekao()) {
            throw new TokenNevalidanException("Token je neispravan ili je istekao.");
        }

        Korisnik k = t.getKorisnik();
        k.setLozinkaHash(encoder.encode(novaLozinka));
        korisnici.save(k);

        t.setKoriscen(true);
        resetTokeni.save(t);

        log.info("Lozinka uspešno resetovana za korisnika (email: {}).", k.getEmail());
    }

}
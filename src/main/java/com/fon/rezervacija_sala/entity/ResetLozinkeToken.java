package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reset_lozinke_token")
public class ResetLozinkeToken {

    @Id
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "korisnikId", nullable = false)
    private Korisnik korisnik;

    @Column(nullable = false)
    private Instant rokTrajanja;

    @Column(nullable = false)
    private boolean koriscen = false;

    public static ResetLozinkeToken of(Korisnik k, long ttlSeconds) {
        ResetLozinkeToken t = new ResetLozinkeToken();
        t.token = UUID.randomUUID().toString();
        t.korisnik = k;
        t.rokTrajanja = Instant.now().plusSeconds(ttlSeconds);
        t.koriscen = false;
        return t;
    }

    public boolean isIstekao() {
        return Instant.now().isAfter(rokTrajanja);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

    public Instant getRokTrajanja() {
        return rokTrajanja;
    }

    public void setRokTrajanja(Instant rokTrajanja) {
        this.rokTrajanja = rokTrajanja;
    }

    public boolean isKoriscen() {
        return koriscen;
    }

    public void setKoriscen(boolean koriscen) {
        this.koriscen = koriscen;
    }

}
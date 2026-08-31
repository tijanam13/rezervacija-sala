package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verifikacioni_token")
public class VerifikacioniToken {

    @Id
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "korisnikId", nullable = false)
    private Korisnik korisnik;

    @Column(nullable = false)
    private Instant rokTrajanja;

    public static VerifikacioniToken of(Korisnik k, long ttlSeconds) {
        VerifikacioniToken t = new VerifikacioniToken();
        t.token = UUID.randomUUID().toString();
        t.korisnik = k;
        t.rokTrajanja = Instant.now().plusSeconds(ttlSeconds);
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

}
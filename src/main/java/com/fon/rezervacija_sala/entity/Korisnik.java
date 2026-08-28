package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "korisnik", uniqueConstraints = {
    @UniqueConstraint(name = "uk_korisnik_email", columnNames = "email")
})
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private String lozinkaHash;

    @Column(nullable = false)
    private LocalDateTime datumRegistracije;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusNaloga status = StatusNaloga.NEAKTIVAN;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "zaposleniId", nullable = false, unique = true)
    private Zaposleni zaposleni;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "korisnik_uloga",
            joinColumns = @JoinColumn(name = "korisnikId"),
            inverseJoinColumns = @JoinColumn(name = "ulogaId")
    )
    private Set<Uloga> uloge = new HashSet<>();

    public Korisnik() {
    }

    public Korisnik(Long id) {
        this.id = id;
    }

    public Korisnik(Long id, String email, String lozinkaHash, LocalDateTime datumRegistracije,
            StatusNaloga status, Zaposleni zaposleni) {
        this.id = id;
        this.email = email;
        this.lozinkaHash = lozinkaHash;
        this.datumRegistracije = datumRegistracije;
        this.status = status;
        this.zaposleni = zaposleni;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLozinkaHash() {
        return lozinkaHash;
    }

    public void setLozinkaHash(String lozinkaHash) {
        this.lozinkaHash = lozinkaHash;
    }

    public LocalDateTime getDatumRegistracije() {
        return datumRegistracije;
    }

    public void setDatumRegistracije(LocalDateTime datumRegistracije) {
        this.datumRegistracije = datumRegistracije;
    }

    public StatusNaloga getStatus() {
        return status;
    }

    public void setStatus(StatusNaloga status) {
        this.status = status;
    }

    public Zaposleni getZaposleni() {
        return zaposleni;
    }

    public void setZaposleni(Zaposleni zaposleni) {
        this.zaposleni = zaposleni;
    }

    public Set<Uloga> getUloge() {
        return uloge;
    }

    public void setUloge(Set<Uloga> uloge) {
        this.uloge = uloge;
    }

    public boolean imaUlogu(NazivUloge naziv) {
        return uloge.stream().anyMatch(u -> u.getNaziv() == naziv);
    }

}
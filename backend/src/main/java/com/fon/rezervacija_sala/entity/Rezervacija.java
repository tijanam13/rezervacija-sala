package com.fon.rezervacija_sala.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rezervacija")
public class Rezervacija {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRezervacije;

    @Version
    private Long verzija;
 
    @Column(nullable = false)
    private LocalDateTime datumKreiranja;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusRezervacije status = StatusRezervacije.NA_CEKANJU;
 
    @Column(length = 500)
    private String napomena;
 
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "korisnikId", nullable = false)
    private Korisnik korisnik;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "svrhaId", nullable = false, unique = true)
    private SvrhaRezervacije svrha;

    @OneToMany(mappedBy = "rezervacija", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StavkaRezervacije> stavke = new ArrayList<>();
 
    public Rezervacija() {
    }
 
    public Rezervacija(Long idRezervacije) {
        this.idRezervacije = idRezervacije;
    }
 
    public Rezervacija(Long idRezervacije, LocalDateTime datumKreiranja, StatusRezervacije status,
                        String napomena, Korisnik korisnik, SvrhaRezervacije svrha) {
        this.idRezervacije = idRezervacije;
        this.datumKreiranja = datumKreiranja;
        this.status = status;
        this.napomena = napomena;
        this.korisnik = korisnik;
        this.svrha = svrha;
    }
 
    public void dodajStavku(StavkaRezervacije item) {
        item.setRezervacija(this);
        this.stavke.add(item);
    }
 
    public void obrisiStavku(StavkaRezervacije item) {
        item.setRezervacija(null);
        this.stavke.remove(item);
    }
 
    public Long getIdRezervacije() {
        return idRezervacije;
    }
 
    public void setIdRezervacije(Long idRezervacije) {
        this.idRezervacije = idRezervacije;
    }

    public Long getVerzija() {
        return verzija;
    }
 
    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }
 
    public void setDatumKreiranja(LocalDateTime datumKreiranja) {
        this.datumKreiranja = datumKreiranja;
    }
 
    public StatusRezervacije getStatus() {
        return status;
    }
 
    public void setStatus(StatusRezervacije status) {
        this.status = status;
    }
 
    public String getNapomena() {
        return napomena;
    }
 
    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }
 
    public Korisnik getKorisnik() {
        return korisnik;
    }
 
    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

    public SvrhaRezervacije getSvrha() {
        return svrha;
    }

    public void setSvrha(SvrhaRezervacije svrha) {
        this.svrha = svrha;
    }
 
    public List<StavkaRezervacije> getStavke() {
        return stavke;
    }
 
    public void setStavke(List<StavkaRezervacije> stavke) {
        this.stavke = stavke;
    }
}
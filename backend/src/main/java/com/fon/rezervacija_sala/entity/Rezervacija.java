package com.fon.rezervacija_sala.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rezervacija")
public class Rezervacija {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long verzija;
 
    @Column(nullable = false)
    private LocalDateTime datumKreiranja;

    @Column(nullable = false)
    private LocalDate datumTermina;

    @Column(nullable = false)
    private LocalTime vremeOd;

    @Column(nullable = false)
    private LocalTime vremeDo;
 
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
        this.id = idRezervacije;
    }
 
    public Rezervacija(Long idRezervacije, LocalDateTime datumKreiranja, LocalDate datumTermina,
                        LocalTime vremeOd, LocalTime vremeDo, StatusRezervacije status,
                        String napomena, Korisnik korisnik, SvrhaRezervacije svrha) {
        this.id = idRezervacije;
        this.datumKreiranja = datumKreiranja;
        this.datumTermina = datumTermina;
        this.vremeOd = vremeOd;
        this.vremeDo = vremeDo;
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
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long idRezervacije) {
        this.id = idRezervacije;
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

    public LocalDate getDatumTermina() {
        return datumTermina;
    }

    public void setDatumTermina(LocalDate datumTermina) {
        this.datumTermina = datumTermina;
    }

    public LocalTime getVremeOd() {
        return vremeOd;
    }

    public void setVremeOd(LocalTime vremeOd) {
        this.vremeOd = vremeOd;
    }

    public LocalTime getVremeDo() {
        return vremeDo;
    }

    public void setVremeDo(LocalTime vremeDo) {
        this.vremeDo = vremeDo;
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
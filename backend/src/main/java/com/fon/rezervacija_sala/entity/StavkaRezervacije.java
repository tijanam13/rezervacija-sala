package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stavka_rezervacije")
public class StavkaRezervacije {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long verzija;
 
    @Column(nullable = false)
    private Integer brojOsoba;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusStavke statusStavke = StatusStavke.NA_CEKANJU;
 
    @Column(length = 500)
    private String opis;
 
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rezervacijaId", nullable = false)
    private Rezervacija rezervacija;
 
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "salaId", nullable = false)
    private Sala sala;
 
    public StavkaRezervacije() {
    }
 
    public StavkaRezervacije(Long id) {
        this.id = id;
    }
 
    public StavkaRezervacije(Long id, Integer brojOsoba, StatusStavke statusStavke, String opis,
                              Rezervacija rezervacija, Sala sala) {
        this.id = id;
        this.brojOsoba = brojOsoba;
        this.statusStavke = statusStavke;
        this.opis = opis;
        this.rezervacija = rezervacija;
        this.sala = sala;
    }
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }

    public Long getVerzija() {
        return verzija;
    }
 
    public Integer getBrojOsoba() {
        return brojOsoba;
    }
 
    public void setBrojOsoba(Integer brojOsoba) {
        this.brojOsoba = brojOsoba;
    }
 
    public StatusStavke getStatusStavke() {
        return statusStavke;
    }
 
    public void setStatusStavke(StatusStavke statusStavke) {
        this.statusStavke = statusStavke;
    }
 
    public String getOpis() {
        return opis;
    }
 
    public void setOpis(String opis) {
        this.opis = opis;
    }
 
    public Rezervacija getRezervacija() {
        return rezervacija;
    }
 
    public void setRezervacija(Rezervacija rezervacija) {
        this.rezervacija = rezervacija;
    }
 
    public Sala getSala() {
        return sala;
    }
 
    public void setSala(Sala sala) {
        this.sala = sala;
    }
    
}
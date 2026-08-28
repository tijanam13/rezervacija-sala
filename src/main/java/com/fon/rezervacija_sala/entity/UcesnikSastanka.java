package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ucesnik_sastanka")
public class UcesnikSastanka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String ucesnik;

    @Column(length = 255)
    private String email;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "zaposleniId")
    private Zaposleni zaposleni;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sastanakId", nullable = false)
    private Sastanak sastanak;

    public UcesnikSastanka() {
    }

    public UcesnikSastanka(Long id, String ucesnik, String email, Zaposleni zaposleni) {
        this.id = id;
        this.ucesnik = ucesnik;
        this.email = email;
        this.zaposleni = zaposleni;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUcesnik() {
        return ucesnik;
    }

    public void setUcesnik(String ucesnik) {
        this.ucesnik = ucesnik;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Zaposleni getZaposleni() {
        return zaposleni;
    }

    public void setZaposleni(Zaposleni zaposleni) {
        this.zaposleni = zaposleni;
    }

    public Sastanak getSastanak() {
        return sastanak;
    }

    public void setSastanak(Sastanak sastanak) {
        this.sastanak = sastanak;
    }

    public boolean isInterni() {
        return zaposleni != null;
    }

}
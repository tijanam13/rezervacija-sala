package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "zaposleni")
public abstract class Zaposleni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String ime;

    @Column(nullable = false, length = 100)
    private String prezime;

    @Column(length = 30)
    private String brojTelefona;

    @Column(length = 50)
    private String brojRadneKnjizice;

    public Zaposleni() {
    }

    public Zaposleni(Long id) {
        this.id = id;
    }

    public Zaposleni(Long id, String ime, String prezime, String brojTelefona, String brojRadneKnjizice) {
        this.id = id;
        this.ime = ime;
        this.prezime = prezime;
        this.brojTelefona = brojTelefona;
        this.brojRadneKnjizice = brojRadneKnjizice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getBrojTelefona() {
        return brojTelefona;
    }

    public void setBrojTelefona(String brojTelefona) {
        this.brojTelefona = brojTelefona;
    }

    public String getBrojRadneKnjizice() {
        return brojRadneKnjizice;
    }

    public void setBrojRadneKnjizice(String brojRadneKnjizice) {
        this.brojRadneKnjizice = brojRadneKnjizice;
    }

}
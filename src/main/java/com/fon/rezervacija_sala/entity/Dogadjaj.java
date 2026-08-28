package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dogadjaj")
public class Dogadjaj extends SvrhaRezervacije {

    @Column(nullable = false, length = 200)
    private String naziv;

    @Column(length = 1000)
    private String opis;

    public Dogadjaj() {
    }

    public Dogadjaj(Long id, String naziv, String opis) {
        super(id);
        this.naziv = naziv;
        this.opis = opis;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

}
package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sluzbenik")
public class Sluzbenik extends Zaposleni {

    @Column(length = 100)
    private String pozicija;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sluzbaId", nullable = false)
    private Sluzba sluzba;

    public Sluzbenik() {
    }

    public Sluzbenik(Long id, String ime, String prezime, String brojTelefona, String brojRadneKnjizice,
            String pozicija, Sluzba sluzba) {
        super(id, ime, prezime, brojTelefona, brojRadneKnjizice);
        this.pozicija = pozicija;
        this.sluzba = sluzba;
    }

    public String getPozicija() {
        return pozicija;
    }

    public void setPozicija(String pozicija) {
        this.pozicija = pozicija;
    }

    public Sluzba getSluzba() {
        return sluzba;
    }

    public void setSluzba(Sluzba sluzba) {
        this.sluzba = sluzba;
    }

}
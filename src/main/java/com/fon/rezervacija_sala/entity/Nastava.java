package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nastava")
public class Nastava extends AkademskaAktivnost {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VrstaNastave vrsta;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VrstaVezbi vrstaVezbi;

    public Nastava() {
    }

    public Nastava(Long id, Integer semestar, NivoStudija nivoStudija, VrstaNastave vrsta, VrstaVezbi vrstaVezbi) {
        super(id, semestar, nivoStudija);
        this.vrsta = vrsta;
        this.vrstaVezbi = vrstaVezbi;
    }

    public VrstaNastave getVrsta() {
        return vrsta;
    }

    public void setVrsta(VrstaNastave vrsta) {
        this.vrsta = vrsta;
    }

    public VrstaVezbi getVrstaVezbi() {
        return vrstaVezbi;
    }

    public void setVrstaVezbi(VrstaVezbi vrstaVezbi) {
        this.vrstaVezbi = vrstaVezbi;
    }

}
package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "akademska_aktivnost")
public abstract class AkademskaAktivnost extends SvrhaRezervacije {

    @Column(nullable = false)
    private Integer semestar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NivoStudija nivoStudija;

    public AkademskaAktivnost() {
    }

    public AkademskaAktivnost(Long id, Integer semestar, NivoStudija nivoStudija) {
        super(id);
        this.semestar = semestar;
        this.nivoStudija = nivoStudija;
    }

    public Integer getSemestar() {
        return semestar;
    }

    public void setSemestar(Integer semestar) {
        this.semestar = semestar;
    }

    public NivoStudija getNivoStudija() {
        return nivoStudija;
    }

    public void setNivoStudija(NivoStudija nivoStudija) {
        this.nivoStudija = nivoStudija;
    }

}
package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ispit")
public class Ispit extends AkademskaAktivnost {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipIspita tip;

    public Ispit() {
    }

    public Ispit(Long id, Integer semestar, NivoStudija nivoStudija, TipIspita tip) {
        super(id, semestar, nivoStudija);
        this.tip = tip;
    }

    public TipIspita getTip() {
        return tip;
    }

    public void setTip(TipIspita tip) {
        this.tip = tip;
    }

}
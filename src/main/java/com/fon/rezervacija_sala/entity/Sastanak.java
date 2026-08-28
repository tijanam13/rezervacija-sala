package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sastanak")
public class Sastanak extends SvrhaRezervacije {

    @Column(nullable = false, length = 200)
    private String tema;

    @Column(length = 1000)
    private String napomena;

    @OneToMany(mappedBy = "sastanak", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UcesnikSastanka> ucesnici = new ArrayList<>();

    public Sastanak() {
    }

    public Sastanak(Long id, String tema, String napomena) {
        super(id);
        this.tema = tema;
        this.napomena = napomena;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }

    public List<UcesnikSastanka> getUcesnici() {
        return ucesnici;
    }

    public void setUcesnici(List<UcesnikSastanka> ucesnici) {
        this.ucesnici = ucesnici;
    }

    public void dodajUcesnika(UcesnikSastanka u) {
        ucesnici.add(u);
        u.setSastanak(this);
    }

}
package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "svrha_rezervacije")
public abstract class SvrhaRezervacije {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public SvrhaRezervacije() {
    }

    public SvrhaRezervacije(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
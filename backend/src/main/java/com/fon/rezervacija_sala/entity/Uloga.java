package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "uloga", uniqueConstraints = {
    @UniqueConstraint(name = "uk_uloga_naziv", columnNames = "naziv")
})
public class Uloga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, unique = true)
    private NazivUloge naziv;

    @Column(length = 500)
    private String opis;

    public Uloga() {
    }

    public Uloga(Long id) {
        this.id = id;
    }

    public Uloga(Long id, NazivUloge naziv, String opis) {
        this.id = id;
        this.naziv = naziv;
        this.opis = opis;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NazivUloge getNaziv() {
        return naziv;
    }

    public void setNaziv(NazivUloge naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Uloga)) {
            return false;
        }
        Uloga druga = (Uloga) o;
        return id != null && id.equals(druga.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
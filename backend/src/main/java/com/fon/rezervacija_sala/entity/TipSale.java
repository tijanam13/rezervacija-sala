package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tip_sale")
public class TipSale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 100, unique = true)
    private String naziv;
 
    @Column(length = 500)
    private String opis;
 
    public TipSale() {
    }
 
    public TipSale(Long id) {
        this.id = id;
    }
 
    public TipSale(Long id, String naziv, String opis) {
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
package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sala", uniqueConstraints = {
    @UniqueConstraint(name = "uk_sala_naziv_zgrada", columnNames = {"naziv", "zgrada"})
})
public class Sala {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Version
    private Long verzija;
 
    @Column(nullable = false, length = 100)
    private String naziv;
 
    @Column(nullable = false, length = 100)
    private String zgrada;
 
    @Column(nullable = false)
    private Integer sprat;
 
    @Column(nullable = false)
    private Integer kapacitet;
 
    @Column(nullable = false)
    private Integer brojRacunara;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSale status = StatusSale.SLOBODNA;
 
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tipSaleId", nullable = false)
    private TipSale tipSale;
 
    public Sala() {
    }
 
    public Sala(Long id) {
        this.id = id;
    }
 
    public Sala(Long id, String naziv, String zgrada, Integer sprat, Integer kapacitet,
                Integer brojRacunara, StatusSale status, TipSale tipSale) {
        this.id = id;
        this.naziv = naziv;
        this.zgrada = zgrada;
        this.sprat = sprat;
        this.kapacitet = kapacitet;
        this.brojRacunara = brojRacunara;
        this.status = status;
        this.tipSale = tipSale;
    }
 
    public Long getId() {
        return id;
    }
 
    public void setId(Long id) {
        this.id = id;
    }
 
    public Long getVerzija() {
        return verzija;
    }

    public void setVerzija(Long verzija) {
        this.verzija = verzija;
    }
 
    public String getNaziv() {
        return naziv;
    }
 
    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }
 
    public String getZgrada() {
        return zgrada;
    }
 
    public void setZgrada(String zgrada) {
        this.zgrada = zgrada;
    }
 
    public Integer getSprat() {
        return sprat;
    }
 
    public void setSprat(Integer sprat) {
        this.sprat = sprat;
    }
 
    public Integer getKapacitet() {
        return kapacitet;
    }
 
    public void setKapacitet(Integer kapacitet) {
        this.kapacitet = kapacitet;
    }
 
    public Integer getBrojRacunara() {
        return brojRacunara;
    }
 
    public void setBrojRacunara(Integer brojRacunara) {
        this.brojRacunara = brojRacunara;
    }
 
    public StatusSale getStatus() {
        return status;
    }
 
    public void setStatus(StatusSale status) {
        this.status = status;
    }
 
    public TipSale getTipSale() {
        return tipSale;
    }
 
    public void setTipSale(TipSale tipSale) {
        this.tipSale = tipSale;
    }
}
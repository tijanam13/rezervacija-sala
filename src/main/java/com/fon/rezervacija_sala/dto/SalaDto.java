package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.StatusSale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class SalaDto {

    private Long id;

    @NotBlank(message = "Naziv sale je obavezan.")
    @Size(min = 1, max = 100, message = "Naziv mora imati između 1 i 100 karaktera.")
    private String naziv;

    @NotBlank(message = "Zgrada je obavezna.")
    @Size(min = 1, max = 100, message = "Zgrada mora imati između 1 i 100 karaktera.")
    private String zgrada;

    @NotNull(message = "Sprat je obavezan.")
    private Integer sprat;

    @NotNull(message = "Kapacitet je obavezan.")
    @Positive(message = "Kapacitet mora biti veći od 0.")
    private Integer kapacitet;

    @NotNull(message = "Broj računara je obavezan.")
    @PositiveOrZero(message = "Broj računara ne može biti negativan.")
    private Integer brojRacunara;

    private StatusSale status;

    @NotNull(message = "Tip sale je obavezan.")
    @Valid
    private TipSaleDto tipSale;

    public SalaDto() {
    }

    public SalaDto(Long id, String naziv, String zgrada, Integer sprat, Integer kapacitet,
            Integer brojRacunara, StatusSale status, TipSaleDto tipSale) {
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

    public TipSaleDto getTipSale() {
        return tipSale;
    }

    public void setTipSale(TipSaleDto tipSale) {
        this.tipSale = tipSale;
    }

}
package com.fon.rezervacija_sala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ZvanjeDto {

    private Long id;

    @NotBlank(message = "Naziv zvanja je obavezan.")
    @Size(min = 2, max = 100, message = "Naziv mora imati između 2 i 100 karaktera.")
    private String naziv;

    @Size(max = 500, message = "Opis može imati najviše 500 karaktera.")
    private String opis;

    public ZvanjeDto() {
    }

    public ZvanjeDto(Long id, String naziv, String opis) {
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
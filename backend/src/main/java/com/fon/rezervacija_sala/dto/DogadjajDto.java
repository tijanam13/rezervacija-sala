package com.fon.rezervacija_sala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DogadjajDto extends SvrhaRezervacijeDto {

    @NotBlank(message = "Naziv događaja je obavezan.")
    @Size(max = 200, message = "Naziv može imati najviše 200 karaktera.")
    private String naziv;

    @Size(max = 1000, message = "Opis može imati najviše 1000 karaktera.")
    private String opis;

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
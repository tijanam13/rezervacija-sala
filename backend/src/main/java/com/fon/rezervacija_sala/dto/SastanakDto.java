package com.fon.rezervacija_sala.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SastanakDto extends SvrhaRezervacijeDto {

    @NotBlank(message = "Tema sastanka je obavezna.")
    @Size(max = 200, message = "Tema može imati najviše 200 karaktera.")
    private String tema;

    @Size(max = 1000, message = "Napomena može imati najviše 1000 karaktera.")
    private String napomena;

    @NotEmpty(message = "Sastanak mora imati bar jednog učesnika.")
    @Valid
    private List<UcesnikSastankaDto> ucesnici;

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

    public List<UcesnikSastankaDto> getUcesnici() {
        return ucesnici;
    }

    public void setUcesnici(List<UcesnikSastankaDto> ucesnici) {
        this.ucesnici = ucesnici;
    }

}
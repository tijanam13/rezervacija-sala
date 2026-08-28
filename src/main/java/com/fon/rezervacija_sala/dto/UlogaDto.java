package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.NazivUloge;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UlogaDto {

    private Long id;

    @NotNull(message = "Naziv uloge je obavezan.")
    private NazivUloge naziv;

    @Size(max = 500, message = "Opis može imati najviše 500 karaktera.")
    private String opis;

    public UlogaDto() {
    }

    public UlogaDto(Long id, NazivUloge naziv, String opis) {
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

}
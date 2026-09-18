package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.StatusStavke;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StavkaRezervacijeDto {

    private Long id;

    @NotNull(message = "Broj osoba je obavezan.")
    @Min(value = 1, message = "Broj osoba mora biti najmanje 1.")
    private Integer brojOsoba;

    private StatusStavke statusStavke;

    @Size(max = 500, message = "Opis može imati najviše 500 karaktera.")
    private String opis;

    private Long rezervacijaId;

    @NotNull(message = "Sala je obavezna.")
    private SalaDto sala;

    public StavkaRezervacijeDto() {
    }

    public StavkaRezervacijeDto(Long id, Integer brojOsoba, StatusStavke statusStavke, String opis,
            Long rezervacijaId, SalaDto sala) {
        this.id = id;
        this.brojOsoba = brojOsoba;
        this.statusStavke = statusStavke;
        this.opis = opis;
        this.rezervacijaId = rezervacijaId;
        this.sala = sala;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBrojOsoba() {
        return brojOsoba;
    }

    public void setBrojOsoba(Integer brojOsoba) {
        this.brojOsoba = brojOsoba;
    }

    public StatusStavke getStatusStavke() {
        return statusStavke;
    }

    public void setStatusStavke(StatusStavke statusStavke) {
        this.statusStavke = statusStavke;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public Long getRezervacijaId() {
        return rezervacijaId;
    }

    public void setRezervacijaId(Long rezervacijaId) {
        this.rezervacijaId = rezervacijaId;
    }

    public SalaDto getSala() {
        return sala;
    }

    public void setSala(SalaDto sala) {
        this.sala = sala;
    }

}
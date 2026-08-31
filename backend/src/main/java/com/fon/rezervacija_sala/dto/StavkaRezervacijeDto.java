package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.StatusStavke;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public class StavkaRezervacijeDto {

    private Long id;

    @NotNull(message = "Datum termina je obavezan.")
    private LocalDate datumTermina;

    @NotNull(message = "Vreme početka termina je obavezno.")
    private LocalTime vremeOd;

    @NotNull(message = "Vreme završetka termina je obavezno.")
    private LocalTime vremeDo;

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

    public StavkaRezervacijeDto(Long id, LocalDate datumTermina, LocalTime vremeOd, LocalTime vremeDo,
            Integer brojOsoba, StatusStavke statusStavke, String opis, Long rezervacijaId, SalaDto sala) {
        this.id = id;
        this.datumTermina = datumTermina;
        this.vremeOd = vremeOd;
        this.vremeDo = vremeDo;
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

    public LocalDate getDatumTermina() {
        return datumTermina;
    }

    public void setDatumTermina(LocalDate datumTermina) {
        this.datumTermina = datumTermina;
    }

    public LocalTime getVremeOd() {
        return vremeOd;
    }

    public void setVremeOd(LocalTime vremeOd) {
        this.vremeOd = vremeOd;
    }

    public LocalTime getVremeDo() {
        return vremeDo;
    }

    public void setVremeDo(LocalTime vremeDo) {
        this.vremeDo = vremeDo;
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
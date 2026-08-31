package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.StatusStavke;
import java.time.LocalDate;
import java.time.LocalTime;

public class ZauzetostDto {

    private Long salaId;
    private LocalDate datumTermina;
    private LocalTime vremeOd;
    private LocalTime vremeDo;
    private StatusStavke statusStavke;
    private Integer brojOsoba;
    private String naziv;

    public ZauzetostDto() {
    }

    public ZauzetostDto(Long salaId, LocalDate datumTermina, LocalTime vremeOd,
            LocalTime vremeDo, StatusStavke statusStavke, Integer brojOsoba, String naziv) {
        this.salaId = salaId;
        this.datumTermina = datumTermina;
        this.vremeOd = vremeOd;
        this.vremeDo = vremeDo;
        this.statusStavke = statusStavke;
        this.brojOsoba = brojOsoba;
        this.naziv = naziv;
    }

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
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

    public StatusStavke getStatusStavke() {
        return statusStavke;
    }

    public void setStatusStavke(StatusStavke statusStavke) {
        this.statusStavke = statusStavke;
    }

    public Integer getBrojOsoba() {
        return brojOsoba;
    }

    public void setBrojOsoba(Integer brojOsoba) {
        this.brojOsoba = brojOsoba;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

}
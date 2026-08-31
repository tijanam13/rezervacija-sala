package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.StatusRezervacije;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class RezervacijaDto {

    private Long id;

    private LocalDateTime datumKreiranja;

    private StatusRezervacije status;

    @Size(max = 500, message = "Napomena može imati najviše 500 karaktera.")
    private String napomena;

    private KorisnikDto korisnik;

    @Valid
    @NotNull(message = "Svrha rezervacije je obavezna.")
    private SvrhaRezervacijeDto svrha;

    @Valid
    @NotEmpty(message = "Rezervacija mora imati bar jednu stavku.")
    private List<StavkaRezervacijeDto> stavke;

    public RezervacijaDto() {
    }

    public RezervacijaDto(Long id, LocalDateTime datumKreiranja, StatusRezervacije status, String napomena,
            KorisnikDto korisnik, SvrhaRezervacijeDto svrha, List<StavkaRezervacijeDto> stavke) {
        this.id = id;
        this.datumKreiranja = datumKreiranja;
        this.status = status;
        this.napomena = napomena;
        this.korisnik = korisnik;
        this.svrha = svrha;
        this.stavke = stavke;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setDatumKreiranja(LocalDateTime datumKreiranja) {
        this.datumKreiranja = datumKreiranja;
    }

    public StatusRezervacije getStatus() {
        return status;
    }

    public void setStatus(StatusRezervacije status) {
        this.status = status;
    }

    public String getNapomena() {
        return napomena;
    }

    public void setNapomena(String napomena) {
        this.napomena = napomena;
    }

    public KorisnikDto getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(KorisnikDto korisnik) {
        this.korisnik = korisnik;
    }

    public SvrhaRezervacijeDto getSvrha() {
        return svrha;
    }

    public void setSvrha(SvrhaRezervacijeDto svrha) {
        this.svrha = svrha;
    }

    public List<StavkaRezervacijeDto> getStavke() {
        return stavke;
    }

    public void setStavke(List<StavkaRezervacijeDto> stavke) {
        this.stavke = stavke;
    }

}
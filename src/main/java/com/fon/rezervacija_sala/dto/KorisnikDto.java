package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.dto.enums.TipKorisnika;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import java.util.List;

public class KorisnikDto {

    private Long id;
    private String email;
    private String ime;
    private String prezime;
    private TipKorisnika tipKorisnika;
    private StatusNaloga status;
    private List<String> uloge;

    public KorisnikDto() {
    }

    public KorisnikDto(Long id, String email, String ime, String prezime, TipKorisnika tipKorisnika,
            StatusNaloga status, List<String> uloge) {
        this.id = id;
        this.email = email;
        this.ime = ime;
        this.prezime = prezime;
        this.tipKorisnika = tipKorisnika;
        this.status = status;
        this.uloge = uloge;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public TipKorisnika getTipKorisnika() {
        return tipKorisnika;
    }

    public void setTipKorisnika(TipKorisnika tipKorisnika) {
        this.tipKorisnika = tipKorisnika;
    }

    public StatusNaloga getStatus() {
        return status;
    }

    public void setStatus(StatusNaloga status) {
        this.status = status;
    }

    public List<String> getUloge() {
        return uloge;
    }

    public void setUloge(List<String> uloge) {
        this.uloge = uloge;
    }

}
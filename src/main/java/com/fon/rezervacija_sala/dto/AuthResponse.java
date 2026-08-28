package com.fon.rezervacija_sala.dto;

import java.util.List;

public class AuthResponse {

    private String token;
    private KorisnikDto korisnik;
    private List<String> uloge;

    public AuthResponse() {
    }

    public AuthResponse(String token, KorisnikDto korisnik, List<String> uloge) {
        this.token = token;
        this.korisnik = korisnik;
        this.uloge = uloge;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public KorisnikDto getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(KorisnikDto korisnik) {
        this.korisnik = korisnik;
    }

    public List<String> getUloge() {
        return uloge;
    }

    public void setUloge(List<String> uloge) {
        this.uloge = uloge;
    }

}
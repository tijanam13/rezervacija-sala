package com.fon.rezervacija_sala.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UcesnikSastankaDto {

    private Long id;

    @NotBlank(message = "Naziv učesnika je obavezan.")
    @Size(max = 200, message = "Naziv učesnika može imati najviše 200 karaktera.")
    private String ucesnik;

    @Email(message = "Email nije u ispravnom formatu.")
    private String email;

    private Long zaposleniId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUcesnik() {
        return ucesnik;
    }

    public void setUcesnik(String ucesnik) {
        this.ucesnik = ucesnik;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getZaposleniId() {
        return zaposleniId;
    }

    public void setZaposleniId(Long zaposleniId) {
        this.zaposleniId = zaposleniId;
    }

}
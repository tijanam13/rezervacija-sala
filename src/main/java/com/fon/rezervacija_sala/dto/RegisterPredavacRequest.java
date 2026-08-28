package com.fon.rezervacija_sala.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterPredavacRequest {

    @NotBlank(message = "Ime je obavezno.")
    @Size(min = 2, max = 100, message = "Ime mora imati između 2 i 100 karaktera.")
    private String ime;

    @NotBlank(message = "Prezime je obavezno.")
    @Size(min = 2, max = 100, message = "Prezime mora imati između 2 i 100 karaktera.")
    private String prezime;

    @Size(max = 30, message = "Broj telefona može imati najviše 30 karaktera.")
    private String brojTelefona;

    @NotBlank(message = "Broj radne knjižice je obavezan.")
    @Size(max = 50, message = "Broj radne knjižice može imati najviše 50 karaktera.")
    private String brojRadneKnjizice;

    @NotBlank(message = "Email je obavezan.")
    @Email(message = "Email nije u ispravnom formatu.")
    private String email;

    @NotBlank(message = "Lozinka je obavezna.")
    @Size(min = 6, max = 255, message = "Lozinka mora imati najmanje 6 karaktera.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Lozinka mora sadržati bar jedno slovo i jedan broj.")
    private String lozinka;

    @Size(max = 100, message = "Titula može imati najviše 100 karaktera.")
    private String titula;

    @Size(max = 100, message = "Termin konsultacija može imati najviše 100 karaktera.")
    private String terminKonsultacija;

    @NotNull(message = "Katedra je obavezna.")
    private Long katedraId;

    @NotNull(message = "Zvanje je obavezno.")
    private Long zvanjeId;

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

    public String getBrojTelefona() {
        return brojTelefona;
    }

    public void setBrojTelefona(String brojTelefona) {
        this.brojTelefona = brojTelefona;
    }

    public String getBrojRadneKnjizice() {
        return brojRadneKnjizice;
    }

    public void setBrojRadneKnjizice(String brojRadneKnjizice) {
        this.brojRadneKnjizice = brojRadneKnjizice;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getTitula() {
        return titula;
    }

    public void setTitula(String titula) {
        this.titula = titula;
    }

    public String getTerminKonsultacija() {
        return terminKonsultacija;
    }

    public void setTerminKonsultacija(String terminKonsultacija) {
        this.terminKonsultacija = terminKonsultacija;
    }

    public Long getKatedraId() {
        return katedraId;
    }

    public void setKatedraId(Long katedraId) {
        this.katedraId = katedraId;
    }

    public Long getZvanjeId() {
        return zvanjeId;
    }

    public void setZvanjeId(Long zvanjeId) {
        this.zvanjeId = zvanjeId;
    }

}
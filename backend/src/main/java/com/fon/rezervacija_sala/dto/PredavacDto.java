package com.fon.rezervacija_sala.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PredavacDto {

    private Long id;

    @NotBlank(message = "Ime je obavezno.")
    @Size(min = 2, max = 100, message = "Ime mora imati između 2 i 100 karaktera.")
    private String ime;

    @NotBlank(message = "Prezime je obavezno.")
    @Size(min = 2, max = 100, message = "Prezime mora imati između 2 i 100 karaktera.")
    private String prezime;

    @Size(max = 30, message = "Broj telefona može imati najviše 30 karaktera.")
    private String brojTelefona;

    @Size(max = 50, message = "Broj radne knjižice može imati najviše 50 karaktera.")
    private String brojRadneKnjizice;

    @jakarta.validation.constraints.Email(message = "Email adresa nije ispravnog formata.")
    @Size(max = 150, message = "Email adresa može imati najviše 150 karaktera.")
    private String poslovniEmail;

    @Size(max = 100, message = "Titula može imati najviše 100 karaktera.")
    private String titula;

    @Size(max = 100, message = "Termin konsultacija može imati najviše 100 karaktera.")
    private String terminKonsultacija;

    @NotNull(message = "Katedra je obavezna.")
    @Valid
    private KatedraDto katedra;

    @NotNull(message = "Zvanje je obavezno.")
    @Valid
    private ZvanjeDto zvanje;

    public PredavacDto() {
    }

    public PredavacDto(Long id, String ime, String prezime, String brojTelefona, String brojRadneKnjizice,
            String titula, String terminKonsultacija, KatedraDto katedra, ZvanjeDto zvanje) {
        this.id = id;
        this.ime = ime;
        this.prezime = prezime;
        this.brojTelefona = brojTelefona;
        this.brojRadneKnjizice = brojRadneKnjizice;
        this.titula = titula;
        this.terminKonsultacija = terminKonsultacija;
        this.katedra = katedra;
        this.zvanje = zvanje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPoslovniEmail() {
        return poslovniEmail;
    }

    public void setPoslovniEmail(String poslovniEmail) {
        this.poslovniEmail = poslovniEmail;
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

    public KatedraDto getKatedra() {
        return katedra;
    }

    public void setKatedra(KatedraDto katedra) {
        this.katedra = katedra;
    }

    public ZvanjeDto getZvanje() {
        return zvanje;
    }

    public void setZvanje(ZvanjeDto zvanje) {
        this.zvanje = zvanje;
    }

}
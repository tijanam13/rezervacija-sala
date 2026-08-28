package com.fon.rezervacija_sala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ZavrsniRadDto extends AkademskaAktivnostDto {

    @NotBlank(message = "Naziv teme je obavezan.")
    @Size(max = 200, message = "Naziv teme može imati najviše 200 karaktera.")
    private String nazivTeme;

    @NotBlank(message = "Ime studenta je obavezno.")
    @Size(max = 200, message = "Ime studenta može imati najviše 200 karaktera.")
    private String student;

    @NotNull(message = "Mentor je obavezan.")
    private PredavacDto mentor;

    @NotEmpty(message = "Komisija mora imati bar jednog člana.")
    @Size(min = 3, max = 3, message = "Komisija mora imati tačno 3 člana.")
    private List<PredavacDto> clanoviKomisije;

    public String getNazivTeme() {
        return nazivTeme;
    }

    public void setNazivTeme(String nazivTeme) {
        this.nazivTeme = nazivTeme;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public PredavacDto getMentor() {
        return mentor;
    }

    public void setMentor(PredavacDto mentor) {
        this.mentor = mentor;
    }

    public List<PredavacDto> getClanoviKomisije() {
        return clanoviKomisije;
    }

    public void setClanoviKomisije(List<PredavacDto> clanoviKomisije) {
        this.clanoviKomisije = clanoviKomisije;
    }

}
package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.NivoStudija;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public abstract class AkademskaAktivnostDto extends SvrhaRezervacijeDto {

    @NotNull(message = "Semestar je obavezan.")
    @Min(value = 1, message = "Semestar mora biti između 1 i 8.")
    @Max(value = 8, message = "Semestar mora biti između 1 i 8.")
    private Integer semestar;

    @NotNull(message = "Nivo studija je obavezan.")
    private NivoStudija nivoStudija;

    public Integer getSemestar() {
        return semestar;
    }

    public void setSemestar(Integer semestar) {
        this.semestar = semestar;
    }

    public NivoStudija getNivoStudija() {
        return nivoStudija;
    }

    public void setNivoStudija(NivoStudija nivoStudija) {
        this.nivoStudija = nivoStudija;
    }

}
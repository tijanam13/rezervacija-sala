package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.TipIspita;
import jakarta.validation.constraints.NotNull;

public class IspitDto extends AkademskaAktivnostDto {

    @NotNull(message = "Tip ispita je obavezan.")
    private TipIspita tipIspita;

    public TipIspita getTipIspita() {
        return tipIspita;
    }

    public void setTipIspita(TipIspita tip) {
        this.tipIspita = tip;
    }

}
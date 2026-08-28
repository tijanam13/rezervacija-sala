package com.fon.rezervacija_sala.dto;

import com.fon.rezervacija_sala.entity.TipIspita;
import jakarta.validation.constraints.NotNull;

public class IspitDto extends AkademskaAktivnostDto {

    @NotNull(message = "Tip ispita je obavezan.")
    private TipIspita tip;

    public TipIspita getTip() {
        return tip;
    }

    public void setTip(TipIspita tip) {
        this.tip = tip;
    }

}
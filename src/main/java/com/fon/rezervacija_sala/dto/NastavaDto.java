package com.fon.rezervacija_sala.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fon.rezervacija_sala.entity.VrstaNastave;
import com.fon.rezervacija_sala.entity.VrstaVezbi;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public class NastavaDto extends AkademskaAktivnostDto {

    @NotNull(message = "Vrsta nastave je obavezna.")
    private VrstaNastave vrsta;
    private VrstaVezbi vrstaVezbi;
    
    @AssertTrue(message = "Vrsta vežbi je obavezna kada je vrsta nastave VEZBE, "
            + "i ne sme biti navedena kada je vrsta nastave PREDAVANJE.")
    @JsonIgnore
    public boolean isVrstaVezbiKonzistentna() {
        if (vrsta == null) {
            return true; 
        }
        if (vrsta == VrstaNastave.VEZBE) {
            return vrstaVezbi != null;
        }
        return vrstaVezbi == null;
    }

    public VrstaNastave getVrsta() {
        return vrsta;
    }

    public void setVrsta(VrstaNastave vrsta) {
        this.vrsta = vrsta;
    }

    public VrstaVezbi getVrstaVezbi() {
        return vrstaVezbi;
    }

    public void setVrstaVezbi(VrstaVezbi vrstaVezbi) {
        this.vrstaVezbi = vrstaVezbi;
    }

}
package com.fon.rezervacija_sala.dto;

import java.util.List;

public class StranicaDto<T> {

    private List<T> sadrzaj;
    private int brojStranice;
    private int velicinaStranice;
    private long ukupnoElemenata;
    private int ukupnoStranica;

    public StranicaDto() {
    }

    public StranicaDto(List<T> sadrzaj, int brojStranice, int velicinaStranice, long ukupnoElemenata) {
        this.sadrzaj = sadrzaj;
        this.brojStranice = brojStranice;
        this.velicinaStranice = velicinaStranice;
        this.ukupnoElemenata = ukupnoElemenata;
        this.ukupnoStranica = velicinaStranice == 0 ? 0
                : (int) Math.ceil((double) ukupnoElemenata / velicinaStranice);
    }

    public List<T> getSadrzaj() {
        return sadrzaj;
    }

    public void setSadrzaj(List<T> sadrzaj) {
        this.sadrzaj = sadrzaj;
    }

    public int getBrojStranice() {
        return brojStranice;
    }

    public void setBrojStranice(int brojStranice) {
        this.brojStranice = brojStranice;
    }

    public int getVelicinaStranice() {
        return velicinaStranice;
    }

    public void setVelicinaStranice(int velicinaStranice) {
        this.velicinaStranice = velicinaStranice;
    }

    public long getUkupnoElemenata() {
        return ukupnoElemenata;
    }

    public void setUkupnoElemenata(long ukupnoElemenata) {
        this.ukupnoElemenata = ukupnoElemenata;
    }

    public int getUkupnoStranica() {
        return ukupnoStranica;
    }

    public void setUkupnoStranica(int ukupnoStranica) {
        this.ukupnoStranica = ukupnoStranica;
    }

}
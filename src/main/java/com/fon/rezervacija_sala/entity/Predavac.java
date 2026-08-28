package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "predavac")
public class Predavac extends Zaposleni {

    @Column(length = 100)
    private String titula;

    @Column(length = 100)
    private String terminKonsultacija;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "katedraId", nullable = false)
    private Katedra katedra;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "zvanjeId", nullable = false)
    private Zvanje zvanje;

    public Predavac() {
    }

    public Predavac(Long id, String ime, String prezime, String brojTelefona, String brojRadneKnjizice,
            String titula, String terminKonsultacija, Katedra katedra, Zvanje zvanje) {
        super(id, ime, prezime, brojTelefona, brojRadneKnjizice);
        this.titula = titula;
        this.terminKonsultacija = terminKonsultacija;
        this.katedra = katedra;
        this.zvanje = zvanje;
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

    public Katedra getKatedra() {
        return katedra;
    }

    public void setKatedra(Katedra katedra) {
        this.katedra = katedra;
    }

    public Zvanje getZvanje() {
        return zvanje;
    }

    public void setZvanje(Zvanje zvanje) {
        this.zvanje = zvanje;
    }

}
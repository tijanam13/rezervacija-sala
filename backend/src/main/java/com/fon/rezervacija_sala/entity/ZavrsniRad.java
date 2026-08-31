package com.fon.rezervacija_sala.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "zavrsni_rad")
public class ZavrsniRad extends AkademskaAktivnost {

    @Column(nullable = false, length = 200)
    private String nazivTeme;

    @Column(nullable = false, length = 200)
    private String student;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "mentorId", nullable = false)
    private Predavac mentor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "zavrsni_rad_komisija",
            joinColumns = @JoinColumn(name = "zavrsniRadId"),
            inverseJoinColumns = @JoinColumn(name = "predavacId")
    )
    private Set<Predavac> clanoviKomisije = new HashSet<>();

    public ZavrsniRad() {
    }

    public ZavrsniRad(Long id, Integer semestar, NivoStudija nivoStudija, String nazivTeme, String student) {
        super(id, semestar, nivoStudija);
        this.nazivTeme = nazivTeme;
        this.student = student;
    }

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

    public Predavac getMentor() {
        return mentor;
    }

    public void setMentor(Predavac mentor) {
        this.mentor = mentor;
    }

    public Set<Predavac> getClanoviKomisije() {
        return clanoviKomisije;
    }

    public void setClanoviKomisije(Set<Predavac> clanoviKomisije) {
        this.clanoviKomisije = clanoviKomisije;
    }

}
package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.DogadjajDto;
import com.fon.rezervacija_sala.dto.IspitDto;
import com.fon.rezervacija_sala.dto.NastavaDto;
import com.fon.rezervacija_sala.dto.PredavacDto;
import com.fon.rezervacija_sala.dto.SastanakDto;
import com.fon.rezervacija_sala.dto.SvrhaRezervacijeDto;
import com.fon.rezervacija_sala.dto.ZavrsniRadDto;
import com.fon.rezervacija_sala.entity.Dogadjaj;
import com.fon.rezervacija_sala.entity.Ispit;
import com.fon.rezervacija_sala.entity.Nastava;
import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.entity.Sastanak;
import com.fon.rezervacija_sala.entity.SvrhaRezervacije;
import com.fon.rezervacija_sala.entity.ZavrsniRad;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SvrhaRezervacijeMapper implements DtoEntityMapper<SvrhaRezervacijeDto, SvrhaRezervacije> {

    private final PredavacMapper predavacMapper;
    private final UcesnikSastankaMapper ucesnikMapper;

    public SvrhaRezervacijeMapper(PredavacMapper predavacMapper, UcesnikSastankaMapper ucesnikMapper) {
        this.predavacMapper = predavacMapper;
        this.ucesnikMapper = ucesnikMapper;
    }

    @Override
    public SvrhaRezervacijeDto toDto(SvrhaRezervacije e) {
        if (e == null) {
            return null;
        }

        if (e instanceof Nastava n) {
            NastavaDto dto = new NastavaDto();
            dto.setId(n.getId());
            dto.setSemestar(n.getSemestar());
            dto.setNivoStudija(n.getNivoStudija());
            dto.setVrsta(n.getVrsta());
            dto.setVrstaVezbi(n.getVrstaVezbi());
            return dto;
        }

        if (e instanceof Ispit i) {
            IspitDto dto = new IspitDto();
            dto.setId(i.getId());
            dto.setSemestar(i.getSemestar());
            dto.setNivoStudija(i.getNivoStudija());
            dto.setTip(i.getTip());
            return dto;
        }

        if (e instanceof ZavrsniRad z) {
            ZavrsniRadDto dto = new ZavrsniRadDto();
            dto.setId(z.getId());
            dto.setSemestar(z.getSemestar());
            dto.setNivoStudija(z.getNivoStudija());
            dto.setNazivTeme(z.getNazivTeme());
            dto.setStudent(z.getStudent());
            dto.setMentor(predavacMapper.toDto(z.getMentor()));
            dto.setClanoviKomisije(predavacMapper.toDtoList(new ArrayList<>(z.getClanoviKomisije())));
            return dto;
        }

        if (e instanceof Sastanak s) {
            SastanakDto dto = new SastanakDto();
            dto.setId(s.getId());
            dto.setTema(s.getTema());
            dto.setNapomena(s.getNapomena());
            dto.setUcesnici(ucesnikMapper.toDtoList(s.getUcesnici()));
            return dto;
        }

        if (e instanceof Dogadjaj d) {
            DogadjajDto dto = new DogadjajDto();
            dto.setId(d.getId());
            dto.setNaziv(d.getNaziv());
            dto.setOpis(d.getOpis());
            return dto;
        }

        throw new IllegalStateException("Nepoznata podvrsta SvrhaRezervacije: " + e.getClass());
    }

    @Override
    public SvrhaRezervacije toEntity(SvrhaRezervacijeDto t) {
        if (t == null) {
            return null;
        }

        if (t instanceof NastavaDto dto) {
            Nastava n = new Nastava();
            n.setId(dto.getId());
            n.setSemestar(dto.getSemestar());
            n.setNivoStudija(dto.getNivoStudija());
            n.setVrsta(dto.getVrsta());
            n.setVrstaVezbi(dto.getVrstaVezbi());
            return n;
        }

        if (t instanceof IspitDto dto) {
            Ispit i = new Ispit();
            i.setId(dto.getId());
            i.setSemestar(dto.getSemestar());
            i.setNivoStudija(dto.getNivoStudija());
            i.setTip(dto.getTip());
            return i;
        }

        if (t instanceof ZavrsniRadDto dto) {
            ZavrsniRad z = new ZavrsniRad();
            z.setId(dto.getId());
            z.setSemestar(dto.getSemestar());
            z.setNivoStudija(dto.getNivoStudija());
            z.setNazivTeme(dto.getNazivTeme());
            z.setStudent(dto.getStudent());

            if (dto.getMentor() != null && dto.getMentor().getId() != null) {
                Predavac mentor = new Predavac();
                mentor.setId(dto.getMentor().getId());
                z.setMentor(mentor);
            }

            Set<Predavac> komisija = new HashSet<>();
            if (dto.getClanoviKomisije() != null) {
                for (PredavacDto pDto : dto.getClanoviKomisije()) {
                    Predavac p = new Predavac();
                    p.setId(pDto.getId());
                    komisija.add(p);
                }
            }
            z.setClanoviKomisije(komisija);
            return z;
        }

        if (t instanceof SastanakDto dto) {
            Sastanak s = new Sastanak();
            s.setId(dto.getId());
            s.setTema(dto.getTema());
            s.setNapomena(dto.getNapomena());

            List<com.fon.rezervacija_sala.entity.UcesnikSastanka> ucesnici = ucesnikMapper.toEntityList(dto.getUcesnici());
            ucesnici.forEach(s::dodajUcesnika);

            return s;
        }

        if (t instanceof DogadjajDto dto) {
            Dogadjaj d = new Dogadjaj();
            d.setId(dto.getId());
            d.setNaziv(dto.getNaziv());
            d.setOpis(dto.getOpis());
            return d;
        }

        throw new IllegalStateException("Nepoznata podvrsta SvrhaRezervacijeDto: " + t.getClass());
    }

}
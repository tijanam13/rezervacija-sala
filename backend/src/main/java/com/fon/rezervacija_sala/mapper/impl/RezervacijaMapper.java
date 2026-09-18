package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.RezervacijaDto;
import com.fon.rezervacija_sala.entity.Rezervacija;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import java.util.Collections;
import org.springframework.stereotype.Component;

@Component
public class RezervacijaMapper implements DtoEntityMapper<RezervacijaDto, Rezervacija> {

    private final StavkaRezervacijeMapper stavkaMapper;
    private final KorisnikMapper korisnikMapper;
    private final SvrhaRezervacijeMapper svrhaMapper;

    public RezervacijaMapper(StavkaRezervacijeMapper stavkaMapper, KorisnikMapper korisnikMapper,
            SvrhaRezervacijeMapper svrhaMapper) {
        this.stavkaMapper = stavkaMapper;
        this.korisnikMapper = korisnikMapper;
        this.svrhaMapper = svrhaMapper;
    }

    @Override
    public RezervacijaDto toDto(Rezervacija e) {
        if (e == null) {
            return null;
        }

        RezervacijaDto dto = new RezervacijaDto();
        dto.setId(e.getId());
        dto.setDatumKreiranja(e.getDatumKreiranja());
        dto.setDatumTermina(e.getDatumTermina());
        dto.setVremeOd(e.getVremeOd());
        dto.setVremeDo(e.getVremeDo());
        dto.setStatus(e.getStatus());
        dto.setNapomena(e.getNapomena());
        dto.setKorisnik(korisnikMapper.toDto(e.getKorisnik()));
        dto.setSvrha(svrhaMapper.toDto(e.getSvrha()));

        if (e.getStavke() != null && !e.getStavke().isEmpty()) {
            dto.setStavke(stavkaMapper.toDtoList(e.getStavke()));
        } else {
            dto.setStavke(Collections.emptyList());
        }

        return dto;
    }

    @Override
    public Rezervacija toEntity(RezervacijaDto t) {
        if (t == null) {
            return null;
        }

        Rezervacija r = new Rezervacija();
        r.setId(t.getId());
        r.setDatumKreiranja(t.getDatumKreiranja());
        r.setDatumTermina(t.getDatumTermina());
        r.setVremeOd(t.getVremeOd());
        r.setVremeDo(t.getVremeDo());
        r.setStatus(t.getStatus());
        r.setNapomena(t.getNapomena());
        r.setSvrha(svrhaMapper.toEntity(t.getSvrha()));

        if (t.getStavke() != null && !t.getStavke().isEmpty()) {
            r.setStavke(stavkaMapper.toEntityList(t.getStavke()));
            r.getStavke().forEach(stavka -> stavka.setRezervacija(r));
        }

        return r;
    }

}
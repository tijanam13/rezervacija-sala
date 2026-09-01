package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.SluzbenikDto;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class SluzbenikMapper implements DtoEntityMapper<SluzbenikDto, Sluzbenik> {

    private final SluzbaMapper sluzbaMapper;

    public SluzbenikMapper(SluzbaMapper sluzbaMapper) {
        this.sluzbaMapper = sluzbaMapper;
    }

    @Override
    public SluzbenikDto toDto(Sluzbenik e) {
        if (e == null) {
            return null;
        }
        SluzbenikDto dto = new SluzbenikDto(
                e.getId(),
                e.getIme(),
                e.getPrezime(),
                e.getBrojTelefona(),
                e.getBrojRadneKnjizice(),
                e.getPozicija(),
                sluzbaMapper.toDto(e.getSluzba())
        );
        dto.setPoslovniEmail(e.getPoslovniEmail());
        return dto;
    }

    @Override
    public Sluzbenik toEntity(SluzbenikDto t) {
        if (t == null) {
            return null;
        }

        Sluzba sluzbaRef = t.getSluzba() != null && t.getSluzba().getId() != null
                ? new Sluzba(t.getSluzba().getId())
                : null;

        Sluzbenik s = new Sluzbenik();
        s.setId(t.getId());
        s.setIme(t.getIme());
        s.setPrezime(t.getPrezime());
        s.setBrojTelefona(t.getBrojTelefona());
        s.setBrojRadneKnjizice(t.getBrojRadneKnjizice());
        s.setPoslovniEmail(t.getPoslovniEmail());
        s.setPozicija(t.getPozicija());
        s.setSluzba(sluzbaRef);
        return s;
    }

}
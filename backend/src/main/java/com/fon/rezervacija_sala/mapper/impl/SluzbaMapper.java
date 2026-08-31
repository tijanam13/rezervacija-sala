package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.SluzbaDto;
import com.fon.rezervacija_sala.entity.Sluzba;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class SluzbaMapper implements DtoEntityMapper<SluzbaDto, Sluzba> {

    @Override
    public SluzbaDto toDto(Sluzba e) {
        if (e == null) {
            return null;
        }
        return new SluzbaDto(e.getId(), e.getNaziv(), e.getOpis());
    }

    @Override
    public Sluzba toEntity(SluzbaDto t) {
        if (t == null) {
            return null;
        }
        return new Sluzba(t.getId(), t.getNaziv(), t.getOpis());
    }

}
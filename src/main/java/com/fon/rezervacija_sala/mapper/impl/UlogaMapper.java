package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.UlogaDto;
import com.fon.rezervacija_sala.entity.Uloga;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class UlogaMapper implements DtoEntityMapper<UlogaDto, Uloga> {

    @Override
    public UlogaDto toDto(Uloga e) {
        if (e == null) {
            return null;
        }
        return new UlogaDto(e.getId(), e.getNaziv(), e.getOpis());
    }

    @Override
    public Uloga toEntity(UlogaDto t) {
        if (t == null) {
            return null;
        }
        return new Uloga(t.getId(), t.getNaziv(), t.getOpis());
    }

}
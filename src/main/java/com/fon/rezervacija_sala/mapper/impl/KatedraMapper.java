package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.KatedraDto;
import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class KatedraMapper implements DtoEntityMapper<KatedraDto, Katedra> {

    @Override
    public KatedraDto toDto(Katedra e) {
        if (e == null) {
            return null;
        }
        return new KatedraDto(e.getId(), e.getNaziv(), e.getOpis());
    }

    @Override
    public Katedra toEntity(KatedraDto t) {
        if (t == null) {
            return null;
        }
        return new Katedra(t.getId(), t.getNaziv(), t.getOpis());
    }

}
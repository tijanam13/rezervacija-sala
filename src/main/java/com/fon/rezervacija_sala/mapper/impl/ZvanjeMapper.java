package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.ZvanjeDto;
import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class ZvanjeMapper implements DtoEntityMapper<ZvanjeDto, Zvanje> {

    @Override
    public ZvanjeDto toDto(Zvanje e) {
        if (e == null) {
            return null;
        }
        return new ZvanjeDto(e.getId(), e.getNaziv(), e.getOpis());
    }

    @Override
    public Zvanje toEntity(ZvanjeDto t) {
        if (t == null) {
            return null;
        }
        return new Zvanje(t.getId(), t.getNaziv(), t.getOpis());
    }

}
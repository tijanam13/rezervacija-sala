package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.UcesnikSastankaDto;
import com.fon.rezervacija_sala.entity.UcesnikSastanka;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class UcesnikSastankaMapper implements DtoEntityMapper<UcesnikSastankaDto, UcesnikSastanka> {

    @Override
    public UcesnikSastankaDto toDto(UcesnikSastanka e) {
        if (e == null) {
            return null;
        }
        UcesnikSastankaDto dto = new UcesnikSastankaDto();
        dto.setId(e.getId());
        dto.setUcesnik(e.getUcesnik());
        dto.setEmail(e.getEmail());
        dto.setZaposleniId(e.getZaposleni() != null ? e.getZaposleni().getId() : null);
        return dto;
    }

    @Override
    public UcesnikSastanka toEntity(UcesnikSastankaDto t) {
        if (t == null) {
            return null;
        }
        UcesnikSastanka u = new UcesnikSastanka();
        u.setId(t.getId());
        u.setUcesnik(t.getUcesnik());
        u.setEmail(t.getEmail());
        return u;
    }

}
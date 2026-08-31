package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.SalaDto;
import com.fon.rezervacija_sala.entity.Sala;
import com.fon.rezervacija_sala.entity.TipSale;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class SalaMapper implements DtoEntityMapper<SalaDto, Sala> {

    private final TipSaleMapper tipSaleMapper;

    public SalaMapper(TipSaleMapper tipSaleMapper) {
        this.tipSaleMapper = tipSaleMapper;
    }

    @Override
    public SalaDto toDto(Sala e) {
        if (e == null) {
            return null;
        }
        return new SalaDto(
                e.getId(),
                e.getNaziv(),
                e.getZgrada(),
                e.getSprat(),
                e.getKapacitet(),
                e.getBrojRacunara(),
                e.getStatus(),
                tipSaleMapper.toDto(e.getTipSale())
        );
    }

    @Override
    public Sala toEntity(SalaDto t) {
        if (t == null) {
            return null;
        }
        
        TipSale tipSaleRef = t.getTipSale() != null && t.getTipSale().getId() != null
                ? new TipSale(t.getTipSale().getId())
                : null;

        return new Sala(
                t.getId(),
                t.getNaziv(),
                t.getZgrada(),
                t.getSprat(),
                t.getKapacitet(),
                t.getBrojRacunara(),
                t.getStatus(),
                tipSaleRef
        );
    }

}
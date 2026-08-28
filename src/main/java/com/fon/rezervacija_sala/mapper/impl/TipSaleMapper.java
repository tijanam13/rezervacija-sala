package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.TipSaleDto;
import com.fon.rezervacija_sala.entity.TipSale;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class TipSaleMapper implements DtoEntityMapper<TipSaleDto, TipSale> {

    @Override
    public TipSaleDto toDto(TipSale e) {
        if (e == null) {
            return null;
        }
        return new TipSaleDto(e.getId(), e.getNaziv(), e.getOpis());
    }

    @Override
    public TipSale toEntity(TipSaleDto t) {
        if (t == null) {
            return null;
        }
        return new TipSale(t.getId(), t.getNaziv(), t.getOpis());
    }

}
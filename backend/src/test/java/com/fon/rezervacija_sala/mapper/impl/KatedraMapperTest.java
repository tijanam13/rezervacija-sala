package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.KatedraDto;
import com.fon.rezervacija_sala.entity.Katedra;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KatedraMapperTest {

    private final KatedraMapper mapper = new KatedraMapper();

    @Test
    void toDto_ispravnoPreslikavaSvaPolja() {
        Katedra entitet = new Katedra(1L, "Katedra za softversko inženjerstvo", "Opis katedre");

        KatedraDto dto = mapper.toDto(entitet);

        assertEquals(1L, dto.getId());
        assertEquals("Katedra za softversko inženjerstvo", dto.getNaziv());
        assertEquals("Opis katedre", dto.getOpis());
    }

    @Test
    void toEntity_ispravnoPreslikavaSvaPolja() {
        KatedraDto dto = new KatedraDto(2L, "Katedra za informacione sisteme", "Opis");

        Katedra entitet = mapper.toEntity(dto);

        assertEquals(2L, entitet.getId());
        assertEquals("Katedra za informacione sisteme", entitet.getNaziv());
        assertEquals("Opis", entitet.getOpis());
    }

    @Test
    void toDto_null_vracaNull() {
        assertNull(mapper.toDto(null));
    }
}
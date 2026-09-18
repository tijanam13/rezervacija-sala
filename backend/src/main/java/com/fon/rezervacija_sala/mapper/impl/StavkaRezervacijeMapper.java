package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.StavkaRezervacijeDto;
import com.fon.rezervacija_sala.entity.Rezervacija;
import com.fon.rezervacija_sala.entity.Sala;
import com.fon.rezervacija_sala.entity.StavkaRezervacije;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class StavkaRezervacijeMapper implements DtoEntityMapper<StavkaRezervacijeDto, StavkaRezervacije> {

    private final SalaMapper salaMapper;

    public StavkaRezervacijeMapper(SalaMapper salaMapper) {
        this.salaMapper = salaMapper;
    }

    @Override
    public StavkaRezervacijeDto toDto(StavkaRezervacije e) {
        if (e == null) {
            return null;
        }
        return new StavkaRezervacijeDto(
                e.getId(),
                e.getBrojOsoba(),
                e.getStatusStavke(),
                e.getOpis(),
                e.getRezervacija() != null ? e.getRezervacija().getId() : null,
                salaMapper.toDto(e.getSala())
        );
    }

    @Override
    public StavkaRezervacije toEntity(StavkaRezervacijeDto t) {
        if (t == null) {
            return null;
        }

        Rezervacija rezervacijaRef = t.getRezervacijaId() != null
                ? new Rezervacija(t.getRezervacijaId())
                : null;
        Sala salaRef = t.getSala() != null && t.getSala().getId() != null
                ? new Sala(t.getSala().getId())
                : null;

        return new StavkaRezervacije(
                t.getId(),
                t.getBrojOsoba(),
                t.getStatusStavke(),
                t.getOpis(),
                rezervacijaRef,
                salaRef
        );
    }

}
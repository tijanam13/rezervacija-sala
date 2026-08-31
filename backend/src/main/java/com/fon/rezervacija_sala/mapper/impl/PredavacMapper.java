package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.PredavacDto;
import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class PredavacMapper implements DtoEntityMapper<PredavacDto, Predavac> {

    private final KatedraMapper katedraMapper;
    private final ZvanjeMapper zvanjeMapper;

    public PredavacMapper(KatedraMapper katedraMapper, ZvanjeMapper zvanjeMapper) {
        this.katedraMapper = katedraMapper;
        this.zvanjeMapper = zvanjeMapper;
    }

    @Override
    public PredavacDto toDto(Predavac e) {
        if (e == null) {
            return null;
        }
        return new PredavacDto(
                e.getId(),
                e.getIme(),
                e.getPrezime(),
                e.getBrojTelefona(),
                e.getBrojRadneKnjizice(),
                e.getTitula(),
                e.getTerminKonsultacija(),
                katedraMapper.toDto(e.getKatedra()),
                zvanjeMapper.toDto(e.getZvanje())
        );
    }

    @Override
    public Predavac toEntity(PredavacDto t) {
        if (t == null) {
            return null;
        }

        Katedra katedraRef = t.getKatedra() != null && t.getKatedra().getId() != null
                ? new Katedra(t.getKatedra().getId())
                : null;
        Zvanje zvanjeRef = t.getZvanje() != null && t.getZvanje().getId() != null
                ? new Zvanje(t.getZvanje().getId())
                : null;

        Predavac p = new Predavac();
        p.setId(t.getId());
        p.setIme(t.getIme());
        p.setPrezime(t.getPrezime());
        p.setBrojTelefona(t.getBrojTelefona());
        p.setBrojRadneKnjizice(t.getBrojRadneKnjizice());
        p.setTitula(t.getTitula());
        p.setTerminKonsultacija(t.getTerminKonsultacija());
        p.setKatedra(katedraRef);
        p.setZvanje(zvanjeRef);
        return p;
    }

}
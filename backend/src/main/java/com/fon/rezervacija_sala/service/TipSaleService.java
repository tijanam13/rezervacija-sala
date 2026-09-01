package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.TipSaleDto;
import com.fon.rezervacija_sala.entity.TipSale;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.TipSaleMapper;
import com.fon.rezervacija_sala.repository.impl.TipSaleRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TipSaleService {

    private static final Logger log = LoggerFactory.getLogger(TipSaleService.class);

    private final TipSaleRepository tipoviSala;
    private final TipSaleMapper mapper;

    public TipSaleService(TipSaleRepository tipoviSala, TipSaleMapper mapper) {
        this.tipoviSala = tipoviSala;
        this.mapper = mapper;
    }

    public List<TipSaleDto> findAll() {
        return mapper.toDtoList(tipoviSala.findAll());
    }

    @Transactional
    public TipSaleDto create(TipSaleDto dto) {
        TipSale t = mapper.toEntity(dto);
        t.setId(null);
        tipoviSala.save(t);
        log.info("Kreiran tip sale (id: {}, naziv: {}).", t.getId(), t.getNaziv());
        return mapper.toDto(t);
    }

    @Transactional
    public TipSaleDto update(Long id, TipSaleDto dto) {
        TipSale postojeci = pronadjiIliBaciGresku(id);
        postojeci.setNaziv(dto.getNaziv());
        postojeci.setOpis(dto.getOpis());
        tipoviSala.save(postojeci);
        log.info("Ažuriran tip sale (id: {}).", id);
        return mapper.toDto(postojeci);
    }

    @Transactional
    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id);
        tipoviSala.deleteById(id);
        log.info("Obrisan tip sale (id: {}).", id);
    }

    private TipSale pronadjiIliBaciGresku(Long id) {
        return tipoviSala.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Tip sale sa id " + id + " ne postoji."));
    }

}
package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.SalaDto;
import com.fon.rezervacija_sala.dto.TipSaleDto;
import com.fon.rezervacija_sala.entity.Sala;
import com.fon.rezervacija_sala.entity.StatusSale;
import com.fon.rezervacija_sala.entity.TipSale;
import com.fon.rezervacija_sala.exception.BrisanjeNijeMoguceException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.SalaMapper;
import com.fon.rezervacija_sala.repository.impl.SalaRepository;
import com.fon.rezervacija_sala.repository.impl.StavkaRezervacijeRepository;
import com.fon.rezervacija_sala.repository.impl.TipSaleRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalaService {

    private static final Logger log = LoggerFactory.getLogger(SalaService.class);

    private final SalaRepository sale;
    private final TipSaleRepository tipoviSala;
    private final StavkaRezervacijeRepository stavkeRepo;
    private final SalaMapper mapper;

    public SalaService(SalaRepository sale, TipSaleRepository tipoviSala,
            StavkaRezervacijeRepository stavkeRepo, SalaMapper mapper) {
        this.sale = sale;
        this.tipoviSala = tipoviSala;
        this.stavkeRepo = stavkeRepo;
        this.mapper = mapper;
    }

    public List<SalaDto> findAll() {
        return mapper.toDtoList(sale.findAll());
    }

    @Transactional
    public SalaDto create(SalaDto dto) {
        TipSale tipSale = pronadjiTipSaleIliBaciGresku(dto.getTipSale());

        Sala s = mapper.toEntity(dto);
        s.setId(null);
        s.setTipSale(tipSale);
        if (s.getStatus() == null) {
            s.setStatus(StatusSale.SLOBODNA);
        }
        sale.save(s);

        log.info("Kreirana sala (id: {}, naziv: {}, zgrada: {}).", s.getId(), s.getNaziv(), s.getZgrada());
        return mapper.toDto(s);
    }

    @Transactional
    public SalaDto update(Long id, SalaDto dto) {
        Sala postojeca = pronadjiIliBaciGresku(id);
        TipSale tipSale = pronadjiTipSaleIliBaciGresku(dto.getTipSale());

        postojeca.setNaziv(dto.getNaziv());
        postojeca.setZgrada(dto.getZgrada());
        postojeca.setSprat(dto.getSprat());
        postojeca.setKapacitet(dto.getKapacitet());
        postojeca.setBrojRacunara(dto.getBrojRacunara());
        postojeca.setTipSale(tipSale);
        if (dto.getStatus() != null) {
            postojeca.setStatus(dto.getStatus());
        }

        sale.save(postojeca);
        log.info("Ažurirana sala (id: {}).", id);
        return mapper.toDto(postojeca);
    }

    @Transactional
    public SalaDto promeniStatus(Long id, StatusSale noviStatus) {
        Sala s = pronadjiIliBaciGresku(id);
        s.setStatus(noviStatus);
        sale.save(s);
        log.info("Promenjen status sale (id: {}) u {}.", id, noviStatus);
        return mapper.toDto(s);
    }

    @Transactional
    public void deleteById(Long id) {
        Sala s = pronadjiIliBaciGresku(id);

        long brojStavki = stavkeRepo.brojStavkiZaSalu(id);
        if (brojStavki > 0) {
            throw new BrisanjeNijeMoguceException(
                    "Sala \"" + s.getNaziv() + "\" se ne može obrisati. Ima "
                    + brojStavki + " rezervacij" + (brojStavki == 1 ? "u" : "e")
                    + " (prošlih ili budućih) koje se na nju oslanjaju.");
        }

        sale.deleteById(id);
        log.info("Obrisana sala (id: {}).", id);
    }

    private Sala pronadjiIliBaciGresku(Long id) {
        return sale.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Sala sa id " + id + " ne postoji."));
    }

    private TipSale pronadjiTipSaleIliBaciGresku(TipSaleDto tipSaleDto) {
        if (tipSaleDto == null || tipSaleDto.getId() == null) {
            throw new ResursNijePronadjenException("Tip sale mora biti prosleđen.");
        }
        return tipoviSala.findById(tipSaleDto.getId())
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Tip sale sa id " + tipSaleDto.getId() + " ne postoji."));
    }

}
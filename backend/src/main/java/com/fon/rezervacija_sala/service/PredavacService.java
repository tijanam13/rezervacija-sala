package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.KatedraDto;
import com.fon.rezervacija_sala.dto.PredavacDto;
import com.fon.rezervacija_sala.dto.ZvanjeDto;
import com.fon.rezervacija_sala.entity.Katedra;
import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.entity.Zvanje;
import com.fon.rezervacija_sala.exception.NevalidanZahtevException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.mapper.impl.PredavacMapper;
import com.fon.rezervacija_sala.repository.impl.KatedraRepository;
import com.fon.rezervacija_sala.repository.impl.PredavacRepository;
import com.fon.rezervacija_sala.repository.impl.ZvanjeRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PredavacService {

    private static final Logger log = LoggerFactory.getLogger(PredavacService.class);

    private final PredavacRepository predavci;
    private final KatedraRepository katedre;
    private final ZvanjeRepository zvanja;
    private final PredavacMapper mapper;

    public PredavacService(PredavacRepository predavci, KatedraRepository katedre,
            ZvanjeRepository zvanja, PredavacMapper mapper) {
        this.predavci = predavci;
        this.katedre = katedre;
        this.zvanja = zvanja;
        this.mapper = mapper;
    }

    public List<PredavacDto> findAll() {
        return mapper.toDtoList(predavci.findAll());
    }

    public PredavacDto findById(Long id) {
        return mapper.toDto(pronadjiIliBaciGresku(id));
    }

    public List<PredavacDto> findByKatedra(Long katedraId) {
        return mapper.toDtoList(predavci.findByKatedra(katedraId));
    }

    public PredavacDto update(Long id, PredavacDto dto) {
        Predavac postojeci = pronadjiIliBaciGresku(id);
        Katedra katedra = pronadjiKatedruIliBaciGresku(dto.getKatedra());
        Zvanje zvanje = pronadjiZvanjeIliBaciGresku(dto.getZvanje());

        postojeci.setIme(dto.getIme());
        postojeci.setPrezime(dto.getPrezime());
        postojeci.setBrojTelefona(dto.getBrojTelefona());
        postojeci.setBrojRadneKnjizice(dto.getBrojRadneKnjizice());
        postojeci.setTitula(dto.getTitula());
        postojeci.setTerminKonsultacija(dto.getTerminKonsultacija());
        postojeci.setKatedra(katedra);
        postojeci.setZvanje(zvanje);

        predavci.save(postojeci);
        log.info("Ažuriran profil Predavača (id: {}).", id);
        return mapper.toDto(postojeci);
    }

    public void deleteById(Long id) {
        pronadjiIliBaciGresku(id);
        predavci.deleteById(id);
        log.info("Obrisan profil Predavača (id: {}).", id);
    }

    private Predavac pronadjiIliBaciGresku(Long id) {
        return predavci.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Predavač sa id " + id + " ne postoji."));
    }

    private Katedra pronadjiKatedruIliBaciGresku(KatedraDto katedraDto) {
        if (katedraDto == null || katedraDto.getId() == null) {
            throw new NevalidanZahtevException("Katedra mora biti prosleđena.");
        }
        return katedre.findById(katedraDto.getId())
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Katedra sa id " + katedraDto.getId() + " ne postoji."));
    }

    private Zvanje pronadjiZvanjeIliBaciGresku(ZvanjeDto zvanjeDto) {
        if (zvanjeDto == null || zvanjeDto.getId() == null) {
            throw new NevalidanZahtevException("Zvanje mora biti prosleđeno.");
        }
        return zvanja.findById(zvanjeDto.getId())
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Zvanje sa id " + zvanjeDto.getId() + " ne postoji."));
    }

}
package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.KorisnikDto;
import com.fon.rezervacija_sala.dto.PredavacDto;
import com.fon.rezervacija_sala.dto.SluzbenikDto;
import com.fon.rezervacija_sala.dto.enums.TipKorisnika;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.entity.Sluzbenik;
import com.fon.rezervacija_sala.entity.Zaposleni;
import com.fon.rezervacija_sala.mapper.DtoEntityMapper;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KorisnikMapper implements DtoEntityMapper<KorisnikDto, Korisnik> {

    private final PredavacMapper predavacMapper;
    private final SluzbenikMapper sluzbenikMapper;

    public KorisnikMapper(PredavacMapper predavacMapper, SluzbenikMapper sluzbenikMapper) {
        this.predavacMapper = predavacMapper;
        this.sluzbenikMapper = sluzbenikMapper;
    }

    @Override
    public KorisnikDto toDto(Korisnik k) {
        if (k == null) {
            return null;
        }

        Zaposleni z = k.getZaposleni();
        TipKorisnika tip = z instanceof Predavac ? TipKorisnika.PREDAVAC
                : z instanceof Sluzbenik ? TipKorisnika.SLUZBENIK
                : null;

        List<String> uloge = k.getUloge().stream()
                .map(u -> u.getNaziv().name())
                .toList();

        PredavacDto predavacDto = z instanceof Predavac p
                ? predavacMapper.toDto(p) : null;
        SluzbenikDto sluzbenikDto = z instanceof Sluzbenik s
                ? sluzbenikMapper.toDto(s) : null;

        return new KorisnikDto(
                k.getId(),
                k.getEmail(),
                z != null ? z.getIme() : null,
                z != null ? z.getPrezime() : null,
                tip,
                k.getStatus(),
                uloge,
                predavacDto,
                sluzbenikDto
        );
    }

    @Override
    public Korisnik toEntity(KorisnikDto t) {
        if (t == null) {
            return null;
        }
        Korisnik k = new Korisnik();
        k.setId(t.getId());
        k.setEmail(t.getEmail());
        k.setStatus(t.getStatus());
        return k;
    }

}
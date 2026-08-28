package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.RezervacijaDto;
import com.fon.rezervacija_sala.dto.SastanakDto;
import com.fon.rezervacija_sala.dto.StavkaRezervacijeDto;
import com.fon.rezervacija_sala.dto.SvrhaRezervacijeDto;
import com.fon.rezervacija_sala.dto.UcesnikSastankaDto;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.entity.Predavac;
import com.fon.rezervacija_sala.entity.Rezervacija;
import com.fon.rezervacija_sala.entity.Sala;
import com.fon.rezervacija_sala.entity.Sastanak;
import com.fon.rezervacija_sala.entity.StatusRezervacije;
import com.fon.rezervacija_sala.entity.StatusSale;
import com.fon.rezervacija_sala.entity.StatusStavke;
import com.fon.rezervacija_sala.entity.StavkaRezervacije;
import com.fon.rezervacija_sala.entity.SvrhaRezervacije;
import com.fon.rezervacija_sala.entity.UcesnikSastanka;
import com.fon.rezervacija_sala.entity.Zaposleni;
import com.fon.rezervacija_sala.entity.ZavrsniRad;
import com.fon.rezervacija_sala.exception.NevalidanZahtevException;
import com.fon.rezervacija_sala.exception.PristupOdbijenException;
import com.fon.rezervacija_sala.exception.ResursNijePronadjenException;
import com.fon.rezervacija_sala.exception.SalaNijeDostupnaException;
import com.fon.rezervacija_sala.exception.StatusTranzicijaNijeDozvoljenaException;
import com.fon.rezervacija_sala.exception.TerminZauzetException;
import com.fon.rezervacija_sala.mapper.impl.RezervacijaMapper;
import com.fon.rezervacija_sala.mapper.impl.SvrhaRezervacijeMapper;
import com.fon.rezervacija_sala.repository.impl.PredavacRepository;
import com.fon.rezervacija_sala.repository.impl.RezervacijaRepository;
import com.fon.rezervacija_sala.repository.impl.SalaRepository;
import com.fon.rezervacija_sala.repository.impl.StavkaRezervacijeRepository;
import com.fon.rezervacija_sala.repository.impl.ZaposleniRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RezervacijaService {

    private static final Logger log = LoggerFactory.getLogger(RezervacijaService.class);

    private final RezervacijaRepository rezervacije;
    private final StavkaRezervacijeRepository stavkeRepo;
    private final SalaRepository sale;
    private final KorisnikService korisnikService;
    private final PredavacRepository predavci;
    private final ZaposleniRepository zaposleni;
    private final RezervacijaMapper mapper;
    private final SvrhaRezervacijeMapper svrhaMapper;

    public RezervacijaService(RezervacijaRepository rezervacije, StavkaRezervacijeRepository stavkeRepo,
            SalaRepository sale, KorisnikService korisnikService, PredavacRepository predavci,
            ZaposleniRepository zaposleni, RezervacijaMapper mapper, SvrhaRezervacijeMapper svrhaMapper) {
        this.rezervacije = rezervacije;
        this.stavkeRepo = stavkeRepo;
        this.sale = sale;
        this.korisnikService = korisnikService;
        this.predavci = predavci;
        this.zaposleni = zaposleni;
        this.mapper = mapper;
        this.svrhaMapper = svrhaMapper;
    }

    public List<RezervacijaDto> findAll() {
        return mapper.toDtoList(rezervacije.findAll());
    }

    public RezervacijaDto findById(Long id) {
        Rezervacija r = pronadjiIliBaciGresku(id);
        proveriVlasnistvoIliOsoblje(r.getKorisnik().getId());
        return mapper.toDto(r);
    }

    public List<RezervacijaDto> findByStatus(StatusRezervacije status) {
        return mapper.toDtoList(rezervacije.findByStatus(status));
    }

    public List<RezervacijaDto> mojeRezervacije() {
        Korisnik k = trenutniKorisnik();
        return mapper.toDtoList(rezervacije.findByKorisnikId(k.getId()));
    }

    public List<RezervacijaDto> findByKorisnikId(Long korisnikId) {
        if (korisnikId == null) {
            throw new NevalidanZahtevException("ID korisnika je obavezan.");
        }

        Korisnik trenutni = trenutniKorisnik();
        boolean jeOsoblje = trenutni.imaUlogu(NazivUloge.KOORDINATOR) || trenutni.imaUlogu(NazivUloge.ADMIN);
        boolean jeVlasnik = Objects.equals(trenutni.getId(), korisnikId);

        if (!jeOsoblje && !jeVlasnik) {
            log.warn("Korisnik (id: {}) je pokušao da pristupi rezervacijama korisnika (id: {}).",
                    trenutni.getId(), korisnikId);
            throw new PristupOdbijenException("Nemate pravo pristupa rezervacijama drugog korisnika.");
        }

        return mapper.toDtoList(rezervacije.findByKorisnikId(korisnikId));
    }


    @Transactional
    public RezervacijaDto create(RezervacijaDto dto) {
        if (dto.getStavke() == null || dto.getStavke().isEmpty()) {
            throw new NevalidanZahtevException("Rezervacija mora imati bar jednu stavku.");
        }
        if (dto.getSvrha() == null) {
            throw new NevalidanZahtevException("Svrha rezervacije je obavezna.");
        }

        Korisnik podnosilac = trenutniKorisnik();

        dto.getStavke().stream()
                .filter(s -> s.getSala() != null && s.getSala().getId() != null)
                .map(s -> s.getSala().getId())
                .distinct()
                .sorted()
                .forEach(salaId -> sale.findByIdForUpdate(salaId)
                .orElseThrow(() -> new ResursNijePronadjenException("Sala sa id " + salaId + " ne postoji.")));

        Rezervacija r = new Rezervacija();
        r.setDatumKreiranja(LocalDateTime.now());
        r.setStatus(StatusRezervacije.NA_CEKANJU);
        r.setNapomena(dto.getNapomena());
        r.setKorisnik(podnosilac);
        r.setSvrha(izgradiIProveriSvrhu(dto.getSvrha()));

        List<StavkaRezervacije> nove = new ArrayList<>();
        for (StavkaRezervacijeDto stavkaDto : dto.getStavke()) {
            StavkaRezervacije stavka = izgradiIProveriStavku(stavkaDto, nove);
            r.dodajStavku(stavka);
            nove.add(stavka);
        }

        rezervacije.save(r);

        log.info("Kreirana rezervacija (id: {}) korisnika (id: {}, email: {}) sa {} stavki/stavkama.",
                r.getIdRezervacije(), podnosilac.getId(), podnosilac.getEmail(), nove.size());

        return mapper.toDto(r);
    }

    private SvrhaRezervacije izgradiIProveriSvrhu(SvrhaRezervacijeDto svrhaDto) {
        SvrhaRezervacije svrha = svrhaMapper.toEntity(svrhaDto);

        if (svrha instanceof ZavrsniRad zavrsniRad) {
            Predavac mentor = zavrsniRad.getMentor();
            if (mentor == null || mentor.getId() == null) {
                throw new NevalidanZahtevException("Mentor je obavezan.");
            }
            if (!predavci.findById(mentor.getId()).isPresent()) {
                throw new ResursNijePronadjenException("Predavac (mentor) sa id " + mentor.getId() + " ne postoji.");
            }

            for (Predavac clan : zavrsniRad.getClanoviKomisije()) {
                if (!predavci.findById(clan.getId()).isPresent()) {
                    throw new ResursNijePronadjenException(
                            "Predavac (član komisije) sa id " + clan.getId() + " ne postoji.");
                }
            }
        }

        if (svrha instanceof Sastanak sastanak && svrhaDto instanceof SastanakDto sastanakDto) {
            povezriInterneUcesnike(sastanak, sastanakDto);
        }

        return svrha;
    }

    private void povezriInterneUcesnike(Sastanak sastanak, SastanakDto sastanakDto) {
        List<UcesnikSastanka> ucesniciEntiteta = sastanak.getUcesnici();
        List<UcesnikSastankaDto> ucesniciDto = sastanakDto.getUcesnici();

        for (int i = 0; i < ucesniciEntiteta.size(); i++) {
            Long zaposleniId = ucesniciDto.get(i).getZaposleniId();
            if (zaposleniId == null) {
                continue; 
            }

            Zaposleni z = zaposleni.findById(zaposleniId)
                    .orElseThrow(() -> new ResursNijePronadjenException(
                            "Zaposleni sa id " + zaposleniId + " ne postoji."));

            ucesniciEntiteta.get(i).setZaposleni(z);
        }
    }

    private StavkaRezervacije izgradiIProveriStavku(StavkaRezervacijeDto dto, List<StavkaRezervacije> nakupljene) {
        if (dto.getSala() == null || dto.getSala().getId() == null) {
            throw new NevalidanZahtevException("Svaka stavka rezervacije mora imati navedenu salu.");
        }
        if (dto.getDatumTermina() == null || dto.getVremeOd() == null || dto.getVremeDo() == null) {
            throw new NevalidanZahtevException("Datum termina i vreme početka/završetka su obavezni.");
        }
        if (dto.getBrojOsoba() == null || dto.getBrojOsoba() < 1) {
            throw new NevalidanZahtevException("Broj osoba mora biti najmanje 1.");
        }
        
        Sala sala = sale.findByIdForUpdate(dto.getSala().getId())
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Sala sa id " + dto.getSala().getId() + " ne postoji."));

        if (sala.getStatus() == StatusSale.VAN_UPOTREBE) {
            throw new SalaNijeDostupnaException(
                    "Sala " + sala.getNaziv() + " (" + sala.getZgrada() + ") trenutno nije dostupna za rezervaciju.");
        }

        LocalDate datum = dto.getDatumTermina();
        LocalTime vremeOd = dto.getVremeOd();
        LocalTime vremeDo = dto.getVremeDo();

        if (!vremeOd.isBefore(vremeDo)) {
            throw new NevalidanZahtevException(
                    "Vreme početka termina mora biti pre vremena završetka (sala: " + sala.getNaziv() + ").");
        }
        if (datum.isBefore(LocalDate.now())) {
            throw new NevalidanZahtevException("Datum termina ne može biti u prošlosti.");
        }
        if (datum.isEqual(LocalDate.now()) && vremeOd.isBefore(LocalTime.now())) {
            throw new NevalidanZahtevException("Vreme početka termina ne može biti u prošlosti.");
        }

        if (dto.getBrojOsoba() > sala.getKapacitet()) {
            throw new NevalidanZahtevException(
                    "Broj osoba (" + dto.getBrojOsoba() + ") premašuje kapacitet sale "
                    + sala.getNaziv() + " (" + sala.getKapacitet() + ").");
        }

        if (rezervacije.postojiPreklapanje(sala.getId(), datum, vremeOd, vremeDo, null)) {
            throw new TerminZauzetException(
                    "Sala " + sala.getNaziv() + " je već rezervisana " + datum + " od " + vremeOd + " do " + vremeDo + ".");
        }

        boolean preklapaSeUnutarZahteva = nakupljene.stream().anyMatch(druga
                -> druga.getSala().getId().equals(sala.getId())
                && druga.getDatumTermina().isEqual(datum)
                && vremeOd.isBefore(druga.getVremeDo())
                && druga.getVremeOd().isBefore(vremeDo));
        if (preklapaSeUnutarZahteva) {
            throw new TerminZauzetException(
                    "Zahtev sadrži dve stavke koje se preklapaju za istu salu (" + sala.getNaziv() + ").");
        }

        StavkaRezervacije stavka = new StavkaRezervacije();
        stavka.setDatumTermina(datum);
        stavka.setVremeOd(vremeOd);
        stavka.setVremeDo(vremeDo);
        stavka.setBrojOsoba(dto.getBrojOsoba());
        stavka.setOpis(dto.getOpis());
        stavka.setStatusStavke(StatusStavke.NA_CEKANJU);
        stavka.setSala(sala);
        return stavka;
    }

    @Transactional
    public RezervacijaDto azurirajStatusStavke(Long stavkaId, StatusStavke noviStatus) {
        if (noviStatus == null) {
            throw new NevalidanZahtevException("Novi status stavke je obavezan.");
        }
        if (noviStatus == StatusStavke.NA_CEKANJU) {
            throw new NevalidanZahtevException("Stavka se ne može vratiti u status NA_CEKANJU.");
        }

        StavkaRezervacije stavka = stavkeRepo.findById(stavkaId)
                .orElseThrow(() -> new ResursNijePronadjenException("Stavka rezervacije sa id " + stavkaId + " ne postoji."));

        if (stavka.getStatusStavke() != StatusStavke.NA_CEKANJU) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Stavka (id: " + stavkaId + ") je već obrađena (trenutni status: " + stavka.getStatusStavke() + ").");
        }

        if (noviStatus == StatusStavke.ODOBRENA) {
            Sala sala = sale.findByIdForUpdate(stavka.getSala().getId())
                    .orElseThrow(() -> new ResursNijePronadjenException(
                            "Sala sa id " + stavka.getSala().getId() + " ne postoji."));
            boolean sadaZauzeto = rezervacije.postojiPreklapanje(
                    sala.getId(), stavka.getDatumTermina(), stavka.getVremeOd(), stavka.getVremeDo(), stavka.getId());
            if (sadaZauzeto) {
                throw new TerminZauzetException(
                        "Sala " + sala.getNaziv() + " je u međuvremenu rezervisana za traženi termin - "
                        + "stavku nije moguće odobriti.");
            }
        }

        stavka.setStatusStavke(noviStatus);
        stavkeRepo.save(stavka);

        Rezervacija r = stavka.getRezervacija();
        preracunajStatusRezervacije(r);
        rezervacije.save(r);

        log.info("Stavka (id: {}) rezervacije (id: {}) postavljena na status {}. Status rezervacije: {}.",
                stavkaId, r.getIdRezervacije(), noviStatus, r.getStatus());

        return mapper.toDto(r);
    }

    @Transactional
    public RezervacijaDto azurirajStatus(Long id, StatusRezervacije noviStatus) {
        if (noviStatus == null) {
            throw new NevalidanZahtevException("Novi status rezervacije je obavezan.");
        }
        if (noviStatus == StatusRezervacije.DELIMICNO_ODOBRENA) {
            throw new NevalidanZahtevException(
                    "Status DELIMICNO_ODOBRENA se ne postavlja ručno, potrebno je obraditi stavke pojedinačno.");
        }
        if (noviStatus == StatusRezervacije.NA_CEKANJU) {
            throw new NevalidanZahtevException("Rezervacija se ne može ručno vratiti u status NA_CEKANJU.");
        }

        Rezervacija r = pronadjiIliBaciGresku(id);

        if (r.getStatus() == StatusRezervacije.OTKAZANA) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Rezervacija (id: " + id + ") je otkazana i njen status se više ne može menjati.");
        }

        StatusStavke statusStavke = switch (noviStatus) {
            case ODOBRENA -> StatusStavke.ODOBRENA;
            case ODBIJENA -> StatusStavke.ODBIJENA;
            case OTKAZANA -> StatusStavke.OTKAZANA;
            default -> throw new NevalidanZahtevException("Nepodržan status rezervacije: " + noviStatus);
        };

        if (noviStatus == StatusRezervacije.ODOBRENA) {
            r.getStavke().stream()
                    .map(st -> st.getSala().getId())
                    .distinct()
                    .sorted()
                    .forEach(salaId -> sale.findByIdForUpdate(salaId)
                    .orElseThrow(() -> new ResursNijePronadjenException("Sala sa id " + salaId + " ne postoji.")));
        }

        for (StavkaRezervacije stavka : r.getStavke()) {
            boolean vecObradjena = stavka.getStatusStavke() == StatusStavke.OTKAZANA
                    || stavka.getStatusStavke() == StatusStavke.ODBIJENA;
            if (vecObradjena) {
                continue; 
            }
            if (noviStatus == StatusRezervacije.ODOBRENA
                    && rezervacije.postojiPreklapanje(stavka.getSala().getId(), stavka.getDatumTermina(),
                            stavka.getVremeOd(), stavka.getVremeDo(), stavka.getId())) {
                throw new TerminZauzetException(
                        "Sala " + stavka.getSala().getNaziv() + " je u međuvremenu zauzeta za termin "
                        + stavka.getDatumTermina() + " " + stavka.getVremeOd() + "-" + stavka.getVremeDo()
                        + " - rezervaciju nije moguće odobriti u celosti.");
            }
            stavka.setStatusStavke(statusStavke);
        }

        preracunajStatusRezervacije(r);
        rezervacije.save(r);

        log.info("Rezervacija (id: {}) ažurirana na status {}.", id, r.getStatus());

        return mapper.toDto(r);
    }

    private void preracunajStatusRezervacije(Rezervacija r) {
        List<StavkaRezervacije> stavke = r.getStavke();
        if (stavke == null || stavke.isEmpty()) {
            return;
        }

        boolean imaNaCekanju = stavke.stream().anyMatch(s -> s.getStatusStavke() == StatusStavke.NA_CEKANJU);
        if (imaNaCekanju) {
            r.setStatus(StatusRezervacije.NA_CEKANJU);
            return;
        }

        boolean imaOdobrenih = stavke.stream().anyMatch(s -> s.getStatusStavke() == StatusStavke.ODOBRENA);
        boolean imaOdbijenih = stavke.stream().anyMatch(s -> s.getStatusStavke() == StatusStavke.ODBIJENA);
        boolean imaOtkazanih = stavke.stream().anyMatch(s -> s.getStatusStavke() == StatusStavke.OTKAZANA);

        if (imaOdobrenih && (imaOdbijenih || imaOtkazanih)) {
            r.setStatus(StatusRezervacije.DELIMICNO_ODOBRENA);
        } else if (imaOdobrenih) {
            r.setStatus(StatusRezervacije.ODOBRENA);
        } else if (imaOdbijenih) {
            r.setStatus(StatusRezervacije.ODBIJENA);
        } else {
            r.setStatus(StatusRezervacije.OTKAZANA);
        }
    }

    @Transactional
    public RezervacijaDto otkaziRezervaciju(Long id) {
        Rezervacija r = pronadjiIliBaciGresku(id);
        proveriVlasnistvoIliOsoblje(r.getKorisnik().getId());

        if (r.getStatus() == StatusRezervacije.OTKAZANA) {
            throw new StatusTranzicijaNijeDozvoljenaException("Rezervacija (id: " + id + ") je već otkazana.");
        }

        for (StavkaRezervacije stavka : r.getStavke()) {
            if (stavka.getStatusStavke() != StatusStavke.ODBIJENA) {
                stavka.setStatusStavke(StatusStavke.OTKAZANA);
            }
        }
        r.setStatus(StatusRezervacije.OTKAZANA);
        rezervacije.save(r);

        log.info("Rezervacija (id: {}) otkazana.", id);

        return mapper.toDto(r);
    }

    @Transactional
    public RezervacijaDto otkaziStavku(Long stavkaId) {
        StavkaRezervacije stavka = stavkeRepo.findById(stavkaId)
                .orElseThrow(() -> new ResursNijePronadjenException("Stavka rezervacije sa id " + stavkaId + " ne postoji."));

        Rezervacija r = stavka.getRezervacija();
        proveriVlasnistvoIliOsoblje(r.getKorisnik().getId());

        if (stavka.getStatusStavke() == StatusStavke.OTKAZANA) {
            throw new StatusTranzicijaNijeDozvoljenaException("Stavka (id: " + stavkaId + ") je već otkazana.");
        }
        if (stavka.getStatusStavke() == StatusStavke.ODBIJENA) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Stavka (id: " + stavkaId + ") je već odbijena i ne može se otkazati.");
        }

        stavka.setStatusStavke(StatusStavke.OTKAZANA);
        stavkeRepo.save(stavka);

        preracunajStatusRezervacije(r);
        rezervacije.save(r);

        log.info("Stavka (id: {}) rezervacije (id: {}) otkazana.", stavkaId, r.getIdRezervacije());

        return mapper.toDto(r);
    }


    private Rezervacija pronadjiIliBaciGresku(Long id) {
        return rezervacije.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Rezervacija sa id " + id + " ne postoji."));
    }

    private void proveriVlasnistvoIliOsoblje(Long vlasnikId) {
        Korisnik trenutni = trenutniKorisnik();
        boolean jeOsoblje = trenutni.imaUlogu(NazivUloge.KOORDINATOR) || trenutni.imaUlogu(NazivUloge.ADMIN);
        boolean jeVlasnik = Objects.equals(trenutni.getId(), vlasnikId);

        if (!jeOsoblje && !jeVlasnik) {
            throw new PristupOdbijenException("Ova radnja je moguća samo nad sopstvenom rezervacijom.");
        }
    }

    private Korisnik trenutniKorisnik() {
        return korisnikService.trenutniKorisnik();
    }

}
package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.RezervacijaDto;
import com.fon.rezervacija_sala.dto.StranicaDto;
import com.fon.rezervacija_sala.dto.SastanakDto;
import com.fon.rezervacija_sala.dto.StavkaRezervacijeDto;
import com.fon.rezervacija_sala.dto.SvrhaRezervacijeDto;
import com.fon.rezervacija_sala.dto.ZauzetostDto;
import com.fon.rezervacija_sala.dto.UcesnikSastankaDto;
import com.fon.rezervacija_sala.entity.Dogadjaj;
import com.fon.rezervacija_sala.entity.Ispit;
import com.fon.rezervacija_sala.entity.Korisnik;
import com.fon.rezervacija_sala.entity.Nastava;
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
import com.fon.rezervacija_sala.entity.VrstaNastave;
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
import org.springframework.scheduling.annotation.Scheduled;
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
    private final MailService mailService;

    public RezervacijaService(RezervacijaRepository rezervacije, StavkaRezervacijeRepository stavkeRepo,
            SalaRepository sale, KorisnikService korisnikService, PredavacRepository predavci,
            ZaposleniRepository zaposleni, RezervacijaMapper mapper, SvrhaRezervacijeMapper svrhaMapper,
            MailService mailService) {
        this.rezervacije = rezervacije;
        this.stavkeRepo = stavkeRepo;
        this.sale = sale;
        this.korisnikService = korisnikService;
        this.predavci = predavci;
        this.zaposleni = zaposleni;
        this.mapper = mapper;
        this.svrhaMapper = svrhaMapper;
        this.mailService = mailService;
    }

    public StranicaDto<RezervacijaDto> findAll(int stranica, int velicina) {
        List<RezervacijaDto> sadrzaj = mapper.toDtoList(rezervacije.findAllPaged(stranica, velicina));
        long ukupno = rezervacije.brojSvihRezervacija();
        return new StranicaDto<>(sadrzaj, stranica, velicina, ukupno);
    }

    public RezervacijaDto findById(Long id) {
        Rezervacija r = pronadjiIliBaciGresku(id);
        proveriVlasnistvoIliAdministraciju(r.getKorisnik().getId());
        return mapper.toDto(r);
    }

    public StranicaDto<RezervacijaDto> findByStatus(StatusRezervacije status, int stranica, int velicina) {
        List<RezervacijaDto> sadrzaj = mapper.toDtoList(rezervacije.findByStatusPaged(status, stranica, velicina));
        long ukupno = rezervacije.brojRezervacijaPoStatusu(status);
        return new StranicaDto<>(sadrzaj, stranica, velicina, ukupno);
    }

    public StranicaDto<RezervacijaDto> mojeRezervacije(StatusRezervacije status, LocalDate odDatum, LocalDate doDatum,
            int stranica, int velicina) {
        if (odDatum != null && doDatum != null && doDatum.isBefore(odDatum)) {
            throw new NevalidanZahtevException("Krajnji datum ne sme biti pre početnog.");
        }
        Korisnik k = trenutniKorisnik();
        List<RezervacijaDto> sadrzaj = mapper.toDtoList(
                rezervacije.findByKorisnikIdPaged(k.getId(), status, odDatum, doDatum, stranica, velicina));
        long ukupno = rezervacije.brojRezervacijaPoKorisniku(k.getId(), status, odDatum, doDatum);
        return new StranicaDto<>(sadrzaj, stranica, velicina, ukupno);
    }

    public List<ZauzetostDto> pregledZauzetosti(LocalDate odDatum, LocalDate doDatum) {
        if (odDatum == null || doDatum == null) {
            throw new NevalidanZahtevException("Početni i krajnji datum perioda su obavezni.");
        }
        if (doDatum.isBefore(odDatum)) {
            throw new NevalidanZahtevException("Krajnji datum ne sme biti pre početnog.");
        }
        return stavkeRepo.findAktivneStavkeZaPeriod(odDatum, doDatum).stream()
                .map(st -> new ZauzetostDto(
                st.getSala().getId(),
                st.getRezervacija().getDatumTermina(),
                st.getRezervacija().getVremeOd(),
                st.getRezervacija().getVremeDo(),
                st.getStatusStavke(),
                st.getBrojOsoba(),
                nazivZaZauzetost(st.getRezervacija().getSvrha())))
                .toList();
    }
   
    private String opisZauzetostiZaPoruku(Long salaId, LocalDate datum, LocalTime vremeOd,
            LocalTime vremeDo, Long iskljuciStavkuId) {
        List<StavkaRezervacije> preklapajuce = rezervacije.pronadjiPreklapajuceStavke(
                salaId, datum, vremeOd, vremeDo, iskljuciStavkuId);
        if (preklapajuce.isEmpty()) {
            return "Sala je trenutno potpuno slobodna u ovom terminu, ali traženi broj osoba sam po sebi "
                    + "premašuje UKUPAN kapacitet sale (proverite podatke stavke).";
        }
        String spisak = preklapajuce.stream()
                .map(st -> "'" + nazivZaZauzetost(st.getRezervacija().getSvrha()) + "' ("
                        + st.getRezervacija().getVremeOd() + "-" + st.getRezervacija().getVremeDo()
                        + ", " + st.getBrojOsoba() + " osoba)")
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "Trenutno u sali: " + spisak + ".";
    }

    private String nazivZaZauzetost(SvrhaRezervacije svrha) {
        svrha = (SvrhaRezervacije) org.hibernate.Hibernate.unproxy(svrha);
        if (svrha instanceof Nastava n) {
            return n.getVrsta() == VrstaNastave.VEZBE ? "Vežbe" : "Predavanje";
        }
        if (svrha instanceof Ispit) {
            return "Ispit";
        }
        if (svrha instanceof ZavrsniRad zr) {
            return zr.getNazivTeme();
        }
        if (svrha instanceof Sastanak s) {
            return s.getTema();
        }
        if (svrha instanceof Dogadjaj d) {
            return d.getNaziv();
        }
        return "Rezervisano";
    }

    @Transactional
    public RezervacijaDto create(RezervacijaDto dto) {
        if (dto.getStavke() == null || dto.getStavke().isEmpty()) {
            throw new NevalidanZahtevException("Rezervacija mora imati bar jednu stavku.");
        }
        if (dto.getSvrha() == null) {
            throw new NevalidanZahtevException("Svrha rezervacije je obavezna.");
        }

        LocalDate datum = dto.getDatumTermina();
        LocalTime vremeOd = dto.getVremeOd();
        LocalTime vremeDo = dto.getVremeDo();
        validirajTerminRezervacije(datum, vremeOd, vremeDo);

        Korisnik podnosilac = trenutniKorisnik();

        dto.getStavke().stream()
                .filter(s -> s.getSala() != null && s.getSala().getId() != null)
                .map(s -> s.getSala().getId())
                .distinct()
                .sorted()
                .forEach(salaId -> sale.findByIdForUpdate(salaId)
                .orElseThrow(() -> new ResursNijePronadjenException("Sala ne postoji.")));

        Rezervacija r = new Rezervacija();
        r.setDatumKreiranja(LocalDateTime.now());
        r.setDatumTermina(datum);
        r.setVremeOd(vremeOd);
        r.setVremeDo(vremeDo);
        r.setStatus(StatusRezervacije.NA_CEKANJU);
        r.setNapomena(dto.getNapomena());
        r.setKorisnik(podnosilac);
        r.setSvrha(izgradiIProveriSvrhu(dto.getSvrha()));

        List<StavkaRezervacije> nove = new ArrayList<>();
        for (StavkaRezervacijeDto stavkaDto : dto.getStavke()) {
            StavkaRezervacije stavka = izgradiIProveriStavku(stavkaDto, datum, vremeOd, vremeDo, nove);
            r.dodajStavku(stavka);
            nove.add(stavka);
        }

        rezervacije.save(r);

        log.info("Kreirana rezervacija (id: {}) korisnika (id: {}, email: {}) za termin {} {}-{} sa {} stavki/stavkama.",
                r.getId(), podnosilac.getId(), podnosilac.getEmail(), datum, vremeOd, vremeDo, nove.size());

        return mapper.toDto(r);
    }

    private void validirajTerminRezervacije(LocalDate datum, LocalTime vremeOd, LocalTime vremeDo) {
        if (datum == null || vremeOd == null || vremeDo == null) {
            throw new NevalidanZahtevException("Datum termina i vreme početka/završetka su obavezni.");
        }
        if (!vremeOd.isBefore(vremeDo)) {
            throw new NevalidanZahtevException("Vreme početka termina mora biti pre vremena završetka.");
        }
        if (datum.isBefore(LocalDate.now())) {
            throw new NevalidanZahtevException("Datum termina ne može biti u prošlosti.");
        }
        if (datum.isEqual(LocalDate.now()) && vremeOd.isBefore(LocalTime.now())) {
            throw new NevalidanZahtevException("Vreme početka termina ne može biti u prošlosti.");
        }
    }

    private SvrhaRezervacije izgradiIProveriSvrhu(SvrhaRezervacijeDto svrhaDto) {
        SvrhaRezervacije svrha = svrhaMapper.toEntity(svrhaDto);

        if (svrha instanceof ZavrsniRad zavrsniRad) {
            Predavac mentor = zavrsniRad.getMentor();
            if (mentor == null || mentor.getId() == null) {
                throw new NevalidanZahtevException("Mentor je obavezan.");
            }
            if (!predavci.findById(mentor.getId()).isPresent()) {
                throw new ResursNijePronadjenException("Predavac (mentor) ne postoji.");
            }

            for (Predavac clan : zavrsniRad.getClanoviKomisije()) {
                if (!predavci.findById(clan.getId()).isPresent()) {
                    throw new ResursNijePronadjenException(
                            "Predavac (član komisije) ne postoji.");
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
                            "Zaposleni ne postoji."));

            ucesniciEntiteta.get(i).setZaposleni(z);
        }
    }

    private StavkaRezervacije izgradiIProveriStavku(StavkaRezervacijeDto dto, LocalDate datum,
            LocalTime vremeOd, LocalTime vremeDo, List<StavkaRezervacije> nakupljene) {
        if (dto.getSala() == null || dto.getSala().getId() == null) {
            throw new NevalidanZahtevException("Svaka stavka rezervacije mora imati navedenu salu.");
        }
        if (dto.getBrojOsoba() == null || dto.getBrojOsoba() < 1) {
            throw new NevalidanZahtevException("Broj osoba mora biti najmanje 1.");
        }

        Sala sala = sale.findByIdForUpdate(dto.getSala().getId())
                .orElseThrow(() -> new ResursNijePronadjenException(
                        "Sala ne postoji."));

        if (sala.getStatus() == StatusSale.VAN_UPOTREBE) {
            throw new SalaNijeDostupnaException(
                    sala.getNaziv() + " (" + sala.getZgrada() + ") trenutno nije dostupna za rezervaciju.");
        }

        if (dto.getBrojOsoba() > sala.getKapacitet()) {
            throw new NevalidanZahtevException(
                    "Broj osoba (" + dto.getBrojOsoba() + ") premašuje kapacitet sale "
                    + sala.getNaziv() + " (" + sala.getKapacitet() + ").");
        }

        int zauzetoIzBaze = rezervacije.zauzetoOsobaUTerminu(sala.getId(), datum, vremeOd, vremeDo, null);
        if (zauzetoIzBaze + dto.getBrojOsoba() > sala.getKapacitet()) {
            throw new TerminZauzetException(
                    sala.getNaziv() + " nema dovoljno slobodnog mesta za termin "
                    + datum + " " + vremeOd + "-" + vremeDo + " (slobodno: "
                    + Math.max(sala.getKapacitet() - zauzetoIzBaze, 0) + " od " + sala.getKapacitet()
                    + ", traženo: " + dto.getBrojOsoba() + ").");
        }

        int zauzetoUnutarZahteva = nakupljene.stream()
                .filter(druga -> druga.getSala().getId().equals(sala.getId()))
                .mapToInt(StavkaRezervacije::getBrojOsoba)
                .sum();
        if (zauzetoIzBaze + zauzetoUnutarZahteva + dto.getBrojOsoba() > sala.getKapacitet()) {
            throw new TerminZauzetException(
                    "Više stavki u ovom zahtevu odnosi se na istu salu (" + sala.getNaziv()
                    + ") u terminima koji se preklapaju, a zbir broja osoba premašuje njen kapacitet.");
        }

        StavkaRezervacije stavka = new StavkaRezervacije();
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
                .orElseThrow(() -> new ResursNijePronadjenException("Stavka rezervacije ne postoji."));

        Rezervacija rez = stavka.getRezervacija();
        if (stavka.getStatusStavke() == StatusStavke.NA_CEKANJU
                && rez.getDatumTermina().isBefore(LocalDate.now())) {
            stavka.setStatusStavke(StatusStavke.ISTEKLA);
            preracunajStatusRezervacije(rez);
            rezervacije.save(rez);
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Termin za stavku je u međuvremenu istekao (datum "
                    + rez.getDatumTermina() + " je već prošao), pa je automatski označena kao istekla "
                    + "i više se ne može odobriti ni odbiti.");
        }

        if (stavka.getStatusStavke() != StatusStavke.NA_CEKANJU) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Stavka je već obrađena (trenutni status: " + stavka.getStatusStavke() + ").");
        }

        if (noviStatus == StatusStavke.ODOBRENA) {
            Sala sala = sale.findByIdForUpdate(stavka.getSala().getId())
                    .orElseThrow(() -> new ResursNijePronadjenException(
                            "Sala ne postoji."));
            int vecZauzeto = rezervacije.zauzetoOsobaUTerminu(
                    sala.getId(), rez.getDatumTermina(), rez.getVremeOd(), rez.getVremeDo(), stavka.getId());
            if (vecZauzeto + stavka.getBrojOsoba() > sala.getKapacitet()) {
                throw new TerminZauzetException(
                        sala.getNaziv() + " u međuvremenu nema dovoljno slobodnog mesta za ovaj "
                        + "termin (slobodno: " + Math.max(sala.getKapacitet() - vecZauzeto, 0)
                        + " od " + sala.getKapacitet() + ", traženo: " + stavka.getBrojOsoba()
                        + "), pa stavku nije moguće odobriti. " + opisZauzetostiZaPoruku(
                                sala.getId(), rez.getDatumTermina(), rez.getVremeOd(),
                                rez.getVremeDo(), stavka.getId()));
            }
        }

        stavka.setStatusStavke(noviStatus);
        stavkeRepo.save(stavka);

        preracunajStatusRezervacije(rez);
        rezervacije.save(rez);

        log.info("Stavka (id: {}) rezervacije (id: {}) postavljena na status {}. Status rezervacije: {}.",
                stavkaId, rez.getId(), noviStatus, rez.getStatus());

        return mapper.toDto(rez);
    }

    @Transactional
    public RezervacijaDto odbijRezervacijuSaRazlogom(Long id, String razlog) {
        RezervacijaDto dto = azurirajStatus(id, StatusRezervacije.ODBIJENA);
        Rezervacija r = pronadjiIliBaciGresku(id);
        posaljiMejlOOdbijanju(r.getKorisnik(), nazivSvrheZaMejl(r), razlog, null);
        return dto;
    }

    @Transactional
    public RezervacijaDto odbijStavkuSaRazlogom(Long stavkaId, String razlog) {
        StavkaRezervacije stavka = stavkeRepo.findById(stavkaId)
                .orElseThrow(() -> new ResursNijePronadjenException("Stavka rezervacije ne postoji."));
        Rezervacija rezStavke = stavka.getRezervacija();
        String opisTermina = stavka.getSala().getNaziv() + ", " + rezStavke.getDatumTermina()
                + " " + rezStavke.getVremeOd() + "-" + rezStavke.getVremeDo();
        RezervacijaDto dto = azurirajStatusStavke(stavkaId, StatusStavke.ODBIJENA);
        Rezervacija r = stavka.getRezervacija();
        posaljiMejlOOdbijanju(r.getKorisnik(), nazivSvrheZaMejl(r), razlog, opisTermina);
        return dto;
    }

    private String nazivSvrheZaMejl(Rezervacija r) {
        SvrhaRezervacije svrha = (SvrhaRezervacije) org.hibernate.Hibernate.unproxy(r.getSvrha());
        if (svrha instanceof Dogadjaj d) {
            return d.getNaziv();
        }
        if (svrha instanceof Sastanak s) {
            return s.getTema();
        }
        if (svrha instanceof ZavrsniRad z) {
            return "odbrana završnog rada \"" + z.getNazivTeme() + "\"";
        }
        if (svrha instanceof Nastava n) {
            String vrsta = n.getVrsta() == VrstaNastave.PREDAVANJE ? "predavanje" : "vežbe";
            return "nastava (" + vrsta + ")";
        }
        if (svrha instanceof Ispit i) {
            return "ispit (" + i.getTip().name().toLowerCase().replace('_', ' ') + ")";
        }
        return "vaša rezervacija";
    }

    private void posaljiMejlOOdbijanju(Korisnik k, String nazivSvrhe, String razlog, String opisTermina) {
        String ime = k.getZaposleni() != null ? k.getZaposleni().getIme() : "";
        String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <meta name="color-scheme" content="light only">
          <meta name="supported-color-schemes" content="light only">
        </head>
        <body bgcolor="#f4f7fa" style="background-color:#f4f7fa !important;margin:0;padding:0;">
        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" bgcolor="#f4f7fa" style="background-color:#f4f7fa !important;">
          <tr>
            <td align="center" style="padding:24px;">
              <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;">
                <tr>
                  <td bgcolor="#002145" style="background-color:#002145 !important;padding:20px 28px;border-radius:16px 16px 0 0;">
                    <table role="presentation" cellpadding="0" cellspacing="0">
                      <tr>
                        <td bgcolor="#11C098" style="width:40px;height:40px;background-color:#11C098 !important;border-radius:8px;text-align:center;vertical-align:middle;font-family:Arial,sans-serif;font-weight:bold;font-size:14px;color:#002145 !important;">ФОН</td>
                        <td style="padding-left:10px;font-family:Arial,sans-serif;font-size:16px;font-weight:bold;color:#ffffff !important;">Rezervacija sala</td>
                      </tr>
                    </table>
                  </td>
                </tr>
                <tr>
                  <td bgcolor="#ffffff" style="background-color:#ffffff !important;border-radius:0 0 16px 16px;padding:32px 28px;">
                    <h2 style="margin:0 0 12px;color:#002145 !important;font-size:20px;font-family:Arial,sans-serif;">Pozdrav %s,</h2>
                    <p style="margin:0 0 12px;color:#444444 !important;font-size:15px;line-height:1.5;font-family:Arial,sans-serif;">
                      Nažalost, %s ("%s") je odbijena.%s
                    </p>
                    <p style="margin:0 0 6px;color:#444444 !important;font-size:14px;font-family:Arial,sans-serif;font-weight:bold;">Razlog:</p>
                    <p style="margin:0 0 20px;color:#a03330 !important;font-size:14px;line-height:1.5;font-family:Arial,sans-serif;background-color:#fdf0ef;padding:12px;border-radius:8px;">%s</p>
                    <hr style="border:none;border-top:1px solid #eef0f2;margin:20px 0;">
                    <p style="margin:0;font-size:12px;color:#999999 !important;font-family:Arial,sans-serif;">
                      Za dodatna pitanja, obratite se koordinatoru.
                    </p>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        </body>
        </html>
        """.formatted(
                ime,
                opisTermina != null ? "stavka vaše rezervacije" : "vaša rezervacija",
                nazivSvrhe,
                opisTermina != null ? " (" + opisTermina + ")" : "",
                razlog
        );
        mailService.sendHtml(k.getEmail(), "Rezervacija odbijena - Rezervacija sala", html);
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
                    "Rezervacija je otkazana i njen status se više ne može menjati.");
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
                    .orElseThrow(() -> new ResursNijePronadjenException("Sala ne postoji.")));
        }

        boolean terminIstekao = r.getDatumTermina().isBefore(LocalDate.now());

        for (StavkaRezervacije stavka : r.getStavke()) {
            if (stavka.getStatusStavke() == StatusStavke.NA_CEKANJU && terminIstekao) {
                stavka.setStatusStavke(StatusStavke.ISTEKLA);
                continue;
            }
            boolean vecObradjena = stavka.getStatusStavke() == StatusStavke.OTKAZANA
                    || stavka.getStatusStavke() == StatusStavke.ODBIJENA
                    || stavka.getStatusStavke() == StatusStavke.ISTEKLA;
            if (vecObradjena) {
                continue;
            }
            int vecZauzeto = rezervacije.zauzetoOsobaUTerminu(stavka.getSala().getId(), r.getDatumTermina(),
                    r.getVremeOd(), r.getVremeDo(), stavka.getId());
            if (noviStatus == StatusRezervacije.ODOBRENA
                    && vecZauzeto + stavka.getBrojOsoba() > stavka.getSala().getKapacitet()) {
                throw new TerminZauzetException(
                        stavka.getSala().getNaziv() + " u međuvremenu nema dovoljno slobodnog mesta za termin "
                        + r.getDatumTermina() + " " + r.getVremeOd() + "-" + r.getVremeDo()
                        + " (slobodno: " + Math.max(stavka.getSala().getKapacitet() - vecZauzeto, 0)
                        + " od " + stavka.getSala().getKapacitet() + ", traženo: " + stavka.getBrojOsoba()
                        + "), pa rezervaciju nije moguće odobriti u celosti. " + opisZauzetostiZaPoruku(
                                stavka.getSala().getId(), r.getDatumTermina(), r.getVremeOd(),
                                r.getVremeDo(), stavka.getId()));
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
        boolean imaIsteklih = stavke.stream().anyMatch(s -> s.getStatusStavke() == StatusStavke.ISTEKLA);
        boolean sveIstekle = stavke.stream().allMatch(s -> s.getStatusStavke() == StatusStavke.ISTEKLA);

        if (imaOdobrenih && (imaOdbijenih || imaOtkazanih || imaIsteklih)) {
            r.setStatus(StatusRezervacije.DELIMICNO_ODOBRENA);
        } else if (imaOdobrenih) {
            r.setStatus(StatusRezervacije.ODOBRENA);
        } else if (sveIstekle) {
            r.setStatus(StatusRezervacije.ISTEKLA);
        } else if (imaOdbijenih) {
            r.setStatus(StatusRezervacije.ODBIJENA);
        } else {
            r.setStatus(StatusRezervacije.OTKAZANA);
        }
    }

    @Transactional
    public RezervacijaDto otkaziRezervaciju(Long id) {
        Rezervacija r = pronadjiIliBaciGresku(id);
        proveriVlasnistvoIliAdministraciju(r.getKorisnik().getId());
        
        boolean nesteklo = false;
        if (r.getDatumTermina().isBefore(LocalDate.now())) {
            for (StavkaRezervacije stavka : r.getStavke()) {
                if (stavka.getStatusStavke() == StatusStavke.NA_CEKANJU) {
                    stavka.setStatusStavke(StatusStavke.ISTEKLA);
                    nesteklo = true;
                }
            }
        }
        if (nesteklo) {
            preracunajStatusRezervacije(r);
        }

        if (r.getStatus() == StatusRezervacije.OTKAZANA) {
            throw new StatusTranzicijaNijeDozvoljenaException("Rezervacija je već otkazana.");
        }
        if (r.getStatus() == StatusRezervacije.ISTEKLA) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Rezervacija je istekla (datum termina je prošao) i ne može se otkazati.");
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
                .orElseThrow(() -> new ResursNijePronadjenException("Stavka rezervacije ne postoji."));

        Rezervacija r = stavka.getRezervacija();
        proveriVlasnistvoIliAdministraciju(r.getKorisnik().getId());

        if (stavka.getStatusStavke() == StatusStavke.NA_CEKANJU
                && r.getDatumTermina().isBefore(LocalDate.now())) {
            stavka.setStatusStavke(StatusStavke.ISTEKLA);
            preracunajStatusRezervacije(r);
            rezervacije.save(r);
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Termin za stavku je u međuvremenu istekao i više se ne može otkazati.");
        }

        if (stavka.getStatusStavke() == StatusStavke.OTKAZANA) {
            throw new StatusTranzicijaNijeDozvoljenaException("Stavka je već otkazana.");
        }
        if (stavka.getStatusStavke() == StatusStavke.ODBIJENA) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Stavka je već odbijena i ne može se otkazati.");
        }
        if (stavka.getStatusStavke() == StatusStavke.ISTEKLA) {
            throw new StatusTranzicijaNijeDozvoljenaException(
                    "Stavka je istekla (datum termina je prošao) i ne može se otkazati.");
        }

        stavka.setStatusStavke(StatusStavke.OTKAZANA);
        stavkeRepo.save(stavka);

        preracunajStatusRezervacije(r);
        rezervacije.save(r);

        log.info("Stavka (id: {}) rezervacije (id: {}) otkazana.", stavkaId, r.getId());

        return mapper.toDto(r);
    }

    private Rezervacija pronadjiIliBaciGresku(Long id) {
        return rezervacije.findById(id)
                .orElseThrow(() -> new ResursNijePronadjenException("Rezervacija ne postoji."));
    }

    private void proveriVlasnistvoIliAdministraciju(Long vlasnikId) {
        Korisnik trenutni = trenutniKorisnik();
        boolean jeAdministracija = trenutni.imaUlogu(NazivUloge.KOORDINATOR) || trenutni.imaUlogu(NazivUloge.ADMIN);
        boolean jeVlasnik = Objects.equals(trenutni.getId(), vlasnikId);

        if (!jeAdministracija && !jeVlasnik) {
            throw new PristupOdbijenException("Ova radnja je moguća samo nad sopstvenom rezervacijom.");
        }
    }

    private Korisnik trenutniKorisnik() {
        return korisnikService.trenutniKorisnik();
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void oznaciIstekleStavke() {
        List<StavkaRezervacije> istekle = stavkeRepo.findIstekleNaCekanju(LocalDate.now());

        for (StavkaRezervacije stavka : istekle) {
            stavka.setStatusStavke(StatusStavke.ISTEKLA);
            preracunajStatusRezervacije(stavka.getRezervacija());
        }

        if (!istekle.isEmpty()) {
            log.info("Automatski označeno {} isteklih stavki (NA_CEKANJU, datum termina prošao).", istekle.size());
        }
    }

}
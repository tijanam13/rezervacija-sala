package com.fon.rezervacija_sala.service;

import com.fon.rezervacija_sala.dto.*;
import com.fon.rezervacija_sala.entity.*;
import com.fon.rezervacija_sala.exception.*;
import com.fon.rezervacija_sala.mapper.impl.RezervacijaMapper;
import com.fon.rezervacija_sala.mapper.impl.SvrhaRezervacijeMapper;
import com.fon.rezervacija_sala.repository.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RezervacijaServiceTest {

    @Mock
    private RezervacijaRepository rezervacije;
    @Mock
    private StavkaRezervacijeRepository stavkeRepo;
    @Mock
    private SalaRepository sale;
    @Mock
    private KorisnikService korisnikService;
    @Mock
    private PredavacRepository predavci;
    @Mock
    private ZaposleniRepository zaposleni;
    @Mock
    private RezervacijaMapper mapper;
    @Mock
    private SvrhaRezervacijeMapper svrhaMapper;
    @Mock
    private MailService mailService;

    private RezervacijaService servis;

    private Korisnik obicanKorisnik;
    private Korisnik administrator;

    @BeforeEach
    void priprema() {
        servis = new RezervacijaService(rezervacije, stavkeRepo, sale, korisnikService,
                predavci, zaposleni, mapper, svrhaMapper, mailService);

        obicanKorisnik = new Korisnik(1L);
        administrator = new Korisnik(2L) {
            @Override
            public boolean imaUlogu(NazivUloge naziv) {
                return naziv == NazivUloge.ADMIN;
            }
        };
    }

    private Sala napraviSalu(Long id, String naziv, int kapacitet, StatusSale status) {
        Sala s = new Sala();
        s.setId(id);
        s.setNaziv(naziv);
        s.setZgrada("Zgrada A");
        s.setKapacitet(kapacitet);
        s.setStatus(status);
        return s;
    }

    private StavkaRezervacije napraviStavku(Long id, Sala sala, int brojOsoba, StatusStavke status) {
        StavkaRezervacije st = new StavkaRezervacije();
        st.setId(id);
        st.setSala(sala);
        st.setBrojOsoba(brojOsoba);
        st.setStatusStavke(status);
        return st;
    }

    private Rezervacija napraviRezervaciju(Long id, LocalDate datum, Korisnik vlasnik,
            StatusRezervacije status, StavkaRezervacije... stavke) {
        Rezervacija r = new Rezervacija();
        r.setId(id);
        r.setDatumTermina(datum);
        r.setVremeOd(LocalTime.of(10, 0));
        r.setVremeDo(LocalTime.of(11, 0));
        r.setStatus(status);
        r.setKorisnik(vlasnik);
        r.setSvrha(new Dogadjaj(1L, "Test događaj", null));
        for (StavkaRezervacije st : stavke) {
            r.dodajStavku(st);
            st.setRezervacija(r);
        }
        return r;
    }

    private RezervacijaDto validanZahtev(SalaDto salaDto, int brojOsoba) {
        StavkaRezervacijeDto stavkaDto = new StavkaRezervacijeDto();
        stavkaDto.setSala(salaDto);
        stavkaDto.setBrojOsoba(brojOsoba);

        DogadjajDto svrhaDto = new DogadjajDto();
        svrhaDto.setNaziv("Test događaj");

        RezervacijaDto dto = new RezervacijaDto();
        dto.setDatumTermina(LocalDate.now().plusDays(5));
        dto.setVremeOd(LocalTime.of(10, 0));
        dto.setVremeDo(LocalTime.of(11, 0));
        dto.setSvrha(svrhaDto);
        dto.setStavke(List.of(stavkaDto));
        return dto;
    }

    @Nested
    class FindAllTest {

        @Test
        void findAll_vracaStranicuSaSadrzajem() {
            when(rezervacije.findAllPaged(0, 10)).thenReturn(List.of(new Rezervacija()));
            when(rezervacije.brojSvihRezervacija()).thenReturn(1L);
            when(mapper.toDtoList(any())).thenReturn(List.of(new RezervacijaDto()));

            StranicaDto<RezervacijaDto> rezultat = servis.findAll(0, 10);

            assertEquals(1, rezultat.getSadrzaj().size());
            assertEquals(1L, rezultat.getUkupnoElemenata());
        }
    }

    @Nested
    class FindByIdTest {

        @Test
        void findById_sopstvenaRezervacija_vracaJe() {
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU);
            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            assertNotNull(servis.findById(1L));
        }

        @Test
        void findById_nepostojecaRezervacija_bacaIzuzetak() {
            when(rezervacije.findById(99L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.findById(99L));
            assertEquals("Rezervacija ne postoji.", izuzetak.getMessage());
        }

        @Test
        void findById_tudjaRezervacijaBezAdminPrava_bacaIzuzetak() {
            Korisnik drugiKorisnik = new Korisnik(99L);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), drugiKorisnik,
                    StatusRezervacije.NA_CEKANJU);
            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            PristupOdbijenException izuzetak = assertThrows(PristupOdbijenException.class,
                    () -> servis.findById(1L));
            assertTrue(izuzetak.getMessage().contains("sopstvenom rezervacijom"));
        }

        @Test
        void findById_tudjaRezervacijaSaAdminPravima_vracaJe() {
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU);
            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(korisnikService.trenutniKorisnik()).thenReturn(administrator);
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            assertNotNull(servis.findById(1L));
        }
    }

    @Nested
    class FindByStatusTest {

        @Test
        void findByStatus_vracaFiltriranuStranicu() {
            when(rezervacije.findByStatusPaged(StatusRezervacije.NA_CEKANJU, 0, 10))
                    .thenReturn(List.of(new Rezervacija()));
            when(rezervacije.brojRezervacijaPoStatusu(StatusRezervacije.NA_CEKANJU)).thenReturn(1L);
            when(mapper.toDtoList(any())).thenReturn(List.of(new RezervacijaDto()));

            StranicaDto<RezervacijaDto> rezultat = servis.findByStatus(StatusRezervacije.NA_CEKANJU, 0, 10);

            assertEquals(1L, rezultat.getUkupnoElemenata());
        }
    }

    @Nested
    class MojeRezervacijeTest {

        @Test
        void mojeRezervacije_krajnjiDatumPrePocetnog_bacaIzuzetak() {
            LocalDate od = LocalDate.of(2026, 9, 20);
            LocalDate do_ = LocalDate.of(2026, 9, 10);

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.mojeRezervacije(null, od, do_, 0, 10));

            assertEquals("Krajnji datum ne sme biti pre početnog.", izuzetak.getMessage());
        }

        @Test
        void mojeRezervacije_ispravanPeriod_vracaStranicu() {
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(rezervacije.findByKorisnikIdPaged(eq(1L), any(), any(), any(), eq(0), eq(10)))
                    .thenReturn(List.of(new Rezervacija()));
            when(rezervacije.brojRezervacijaPoKorisniku(eq(1L), any(), any(), any())).thenReturn(1L);
            when(mapper.toDtoList(any())).thenReturn(List.of(new RezervacijaDto()));

            StranicaDto<RezervacijaDto> rezultat = servis.mojeRezervacije(null, null, null, 0, 10);

            assertEquals(1L, rezultat.getUkupnoElemenata());
        }
    }

    @Nested
    class PregledZauzetostiTest {

        @Test
        void pregledZauzetosti_nedostajuDatumi_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.pregledZauzetosti(null, LocalDate.now()));

            assertEquals("Početni i krajnji datum perioda su obavezni.", izuzetak.getMessage());
        }

        @Test
        void pregledZauzetosti_krajnjiPrePocetnog_bacaIzuzetak() {
            LocalDate od = LocalDate.of(2026, 9, 20);
            LocalDate do_ = LocalDate.of(2026, 9, 10);

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.pregledZauzetosti(od, do_));
            assertEquals("Krajnji datum ne sme biti pre početnog.", izuzetak.getMessage());
        }

        @Test
        void pregledZauzetosti_ispravanPeriod_vracaListu() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.ODOBRENA);
            napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.ODOBRENA, st);

            LocalDate od = LocalDate.now();
            LocalDate do_ = LocalDate.now().plusDays(7);
            when(stavkeRepo.findAktivneStavkeZaPeriod(od, do_)).thenReturn(List.of(st));

            List<ZauzetostDto> rezultat = servis.pregledZauzetosti(od, do_);

            assertEquals(1, rezultat.size());
            assertEquals(5, rezultat.get(0).getBrojOsoba());
        }
    }

    @Nested
    class CreateTest {

        @Test
        void create_praznaListaStavki_bacaIzuzetak() {
            RezervacijaDto dto = new RezervacijaDto();
            dto.setStavke(new ArrayList<>());

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));

            assertEquals("Rezervacija mora imati bar jednu stavku.", izuzetak.getMessage());
        }

        @Test
        void create_nedostajeSvrha_bacaIzuzetak() {
            StavkaRezervacijeDto stavkaDto = new StavkaRezervacijeDto();
            RezervacijaDto dto = new RezervacijaDto();
            dto.setStavke(List.of(stavkaDto));
            dto.setSvrha(null);

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));
            assertEquals("Svrha rezervacije je obavezna.", izuzetak.getMessage());
        }

        @Test
        void create_vremePocetkaPosleZavrsetka_bacaIzuzetak() {
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 5);
            dto.setVremeOd(LocalTime.of(12, 0));
            dto.setVremeDo(LocalTime.of(11, 0));

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));

            assertEquals("Vreme početka termina mora biti pre vremena završetka.", izuzetak.getMessage());
        }

        @Test
        void create_datumUProslosti_bacaIzuzetak() {
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 5);
            dto.setDatumTermina(LocalDate.now().minusDays(1));

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));
            assertEquals("Datum termina ne može biti u prošlosti.", izuzetak.getMessage());
        }

        @Test
        void create_salaNePostoji_bacaIzuzetak() {
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 5);

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.create(dto));
            assertEquals("Sala ne postoji.", izuzetak.getMessage());
        }

        @Test
        void create_salaVanUpotrebe_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.VAN_UPOTREBE);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 5);

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new Dogadjaj(null, "Test", null));

            SalaNijeDostupnaException izuzetak = assertThrows(SalaNijeDostupnaException.class,
                    () -> servis.create(dto));
            assertTrue(izuzetak.getMessage().contains("trenutno nije dostupna"));
        }

        @Test
        void create_brojOsobaManjiOd1_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 0); 

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala)); 

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));
            assertEquals("Broj osoba mora biti najmanje 1.", izuzetak.getMessage());
}

        @Test
        void create_brojOsobaPremasujeKapacitet_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Kabinet 5", 5, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 10);

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new Dogadjaj(null, "Test", null));

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));
            assertTrue(izuzetak.getMessage().contains("premašuje kapacitet"));
        }

        @ParameterizedTest(name = "broj osoba={0}, kapacitet=5, treba da uspe={1}")
        @CsvSource({
            "4,  true",
            "5,  true",
            "6,  false"
        })
        void create_granicneVrednostiKapaciteta(int brojOsoba, boolean trebaDaUspe) {
            Sala sala = napraviSalu(1L, "Kabinet 5", 5, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, brojOsoba);

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new Dogadjaj(null, "Test", null));
            if (trebaDaUspe) {
                when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), any())).thenReturn(0);
                when(mapper.toDto(any())).thenReturn(new RezervacijaDto());
            }

            if (trebaDaUspe) {
                assertDoesNotThrow(() -> servis.create(dto));
            } else {
                NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                        () -> servis.create(dto));
                assertTrue(izuzetak.getMessage().contains("premašuje kapacitet"));
            }
        }

        @Test
        void create_nemaDovoljnoSlobodnogMestaZbogPostojecihRezervacija_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 5);

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new Dogadjaj(null, "Test", null));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), any())).thenReturn(8);

            TerminZauzetException izuzetak = assertThrows(TerminZauzetException.class,
                    () -> servis.create(dto));
            assertTrue(izuzetak.getMessage().contains("nema dovoljno slobodnog mesta"));
        }

        @Test
        void create_zbirViseStavkiZaIstuSaluPremasujeKapacitet_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 8, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);

            StavkaRezervacijeDto stavka1 = new StavkaRezervacijeDto();
            stavka1.setSala(salaDto);
            stavka1.setBrojOsoba(5);
            StavkaRezervacijeDto stavka2 = new StavkaRezervacijeDto();
            stavka2.setSala(salaDto);
            stavka2.setBrojOsoba(5);

            DogadjajDto svrhaDto = new DogadjajDto();
            svrhaDto.setNaziv("Test");
            RezervacijaDto dto = new RezervacijaDto();
            dto.setDatumTermina(LocalDate.now().plusDays(5));
            dto.setVremeOd(LocalTime.of(10, 0));
            dto.setVremeDo(LocalTime.of(11, 0));
            dto.setSvrha(svrhaDto);
            dto.setStavke(List.of(stavka1, stavka2));

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new Dogadjaj(null, "Test", null));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), any())).thenReturn(0);

            TerminZauzetException izuzetak = assertThrows(TerminZauzetException.class,
                    () -> servis.create(dto));
            assertTrue(izuzetak.getMessage().contains("Više stavki u ovom zahtevu"));
        }

        @Test
        void create_zavrsniRadBezMentora_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            StavkaRezervacijeDto stavkaDto = new StavkaRezervacijeDto();
            stavkaDto.setSala(salaDto);
            stavkaDto.setBrojOsoba(5);

            ZavrsniRadDto svrhaDto = new ZavrsniRadDto();
            RezervacijaDto dto = new RezervacijaDto();
            dto.setDatumTermina(LocalDate.now().plusDays(5));
            dto.setVremeOd(LocalTime.of(10, 0));
            dto.setVremeDo(LocalTime.of(11, 0));
            dto.setSvrha(svrhaDto);
            dto.setStavke(List.of(stavkaDto));

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new ZavrsniRad());

            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.create(dto));
            assertEquals("Mentor je obavezan.", izuzetak.getMessage());
        }

        @Test
        void create_sastanakSaNepostojecimZaposlenim_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            StavkaRezervacijeDto stavkaDto = new StavkaRezervacijeDto();
            stavkaDto.setSala(salaDto);
            stavkaDto.setBrojOsoba(5);

            UcesnikSastankaDto ucesnikDto = new UcesnikSastankaDto();
            ucesnikDto.setZaposleniId(999L);
            SastanakDto svrhaDto = new SastanakDto();
            svrhaDto.setUcesnici(List.of(ucesnikDto));

            RezervacijaDto dto = new RezervacijaDto();
            dto.setDatumTermina(LocalDate.now().plusDays(5));
            dto.setVremeOd(LocalTime.of(10, 0));
            dto.setVremeDo(LocalTime.of(11, 0));
            dto.setSvrha(svrhaDto);
            dto.setStavke(List.of(stavkaDto));

            UcesnikSastanka ucesnikEntitet = new UcesnikSastanka();
            Sastanak sastanakEntitet = new Sastanak(null, "Tema sastanka", null);
            sastanakEntitet.setUcesnici(new ArrayList<>(List.of(ucesnikEntitet)));

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(sastanakEntitet);
            when(zaposleni.findById(999L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.create(dto));
            assertEquals("Zaposleni ne postoji.", izuzetak.getMessage());
        }

        @Test
        void create_ispravanZahtev_uspesnoKreira() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            SalaDto salaDto = new SalaDto();
            salaDto.setId(1L);
            RezervacijaDto dto = validanZahtev(salaDto, 5);

            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(svrhaMapper.toEntity(any())).thenReturn(new Dogadjaj(null, "Test", null));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), any())).thenReturn(0);
            when(mapper.toDto(any())).thenReturn(new RezervacijaDto());

            assertNotNull(servis.create(dto));
            verify(rezervacije, times(1)).save(any());
        }
    }

    @Nested
    class AzurirajStatusStavkeTest {

        @Test
        void azurirajStatusStavke_noviStatusNull_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.azurirajStatusStavke(1L, null));
            assertEquals("Novi status stavke je obavezan.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatusStavke_pokusajVracanjaNaCekanje_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.azurirajStatusStavke(1L, StatusStavke.NA_CEKANJU));
            assertEquals("Stavka se ne može vratiti u status NA_CEKANJU.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatusStavke_stavkaNePostoji_bacaIzuzetak() {
            when(stavkeRepo.findById(1L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.azurirajStatusStavke(1L, StatusStavke.ODOBRENA));
            assertEquals("Stavka rezervacije ne postoji.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatusStavke_terminUMedjuvremenuIstekao_oznacavaIsteklomIBacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            napraviRezervaciju(1L, LocalDate.now().minusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.azurirajStatusStavke(1L, StatusStavke.ODOBRENA));

            assertTrue(izuzetak.getMessage().contains("istekao"));
            assertEquals(StatusStavke.ISTEKLA, st.getStatusStavke());
        }

        @Test
        void azurirajStatusStavke_vecObradjenaStavka_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.ODBIJENA);
            napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.ODBIJENA, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.azurirajStatusStavke(1L, StatusStavke.ODOBRENA));

            assertTrue(izuzetak.getMessage().contains("već obrađena"));
        }

        @Test
        void azurirajStatusStavke_odobravanjeBezDovoljnoKapaciteta_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), eq(1L))).thenReturn(8);

            TerminZauzetException izuzetak = assertThrows(TerminZauzetException.class,
                    () -> servis.azurirajStatusStavke(1L, StatusStavke.ODOBRENA));
            assertTrue(izuzetak.getMessage().contains("nema dovoljno slobodnog mesta"));
        }

        @Test
        void azurirajStatusStavke_uspesnoOdobravanje() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), eq(1L))).thenReturn(0);
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            assertNotNull(servis.azurirajStatusStavke(1L, StatusStavke.ODOBRENA));
            assertEquals(StatusStavke.ODOBRENA, st.getStatusStavke());
            assertEquals(StatusRezervacije.ODOBRENA, r.getStatus());
        }
    }

    @Nested
    class OdbijanjeTest {

        @Test
        void odbijRezervacijuSaRazlogom_uspesnoOdbijaISaljeMejl() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);
            obicanKorisnik.setEmail("test@fon.bg.ac.rs");

            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            servis.odbijRezervacijuSaRazlogom(1L, "Sala je zauzeta za renoviranje.");

            verify(mailService, times(1)).sendHtml(eq("test@fon.bg.ac.rs"), any(), any());
            assertEquals(StatusStavke.ODBIJENA, st.getStatusStavke());
        }

        @Test
        void odbijStavkuSaRazlogom_uspesnoOdbijaISaljeMejl() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);
            obicanKorisnik.setEmail("test@fon.bg.ac.rs");

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            servis.odbijStavkuSaRazlogom(1L, "Termin se preklapa.");

            verify(mailService, times(1)).sendHtml(eq("test@fon.bg.ac.rs"), any(), any());
            assertEquals(StatusStavke.ODBIJENA, st.getStatusStavke());
        }
    }

    @Nested
    class AzurirajStatusTest {

        @Test
        void azurirajStatus_noviStatusNull_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.azurirajStatus(1L, null));
            assertEquals("Novi status rezervacije je obavezan.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatus_pokusajDelimicnoOdobrena_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.azurirajStatus(1L, StatusRezervacije.DELIMICNO_ODOBRENA));
            assertTrue(izuzetak.getMessage().contains("DELIMICNO_ODOBRENA se ne postavlja ručno"));
        }

        @Test
        void azurirajStatus_pokusajNaCekanju_bacaIzuzetak() {
            NevalidanZahtevException izuzetak = assertThrows(NevalidanZahtevException.class,
                    () -> servis.azurirajStatus(1L, StatusRezervacije.NA_CEKANJU));
            assertEquals("Rezervacija se ne može ručno vratiti u status NA_CEKANJU.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatus_rezervacijaNePostoji_bacaIzuzetak() {
            when(rezervacije.findById(1L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.azurirajStatus(1L, StatusRezervacije.ODOBRENA));
            assertEquals("Rezervacija ne postoji.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatus_vecOtkazana_bacaIzuzetak() {
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.OTKAZANA);
            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.azurirajStatus(1L, StatusRezervacije.ODOBRENA));

            assertEquals("Rezervacija je otkazana i njen status se više ne može menjati.", izuzetak.getMessage());
        }

        @Test
        void azurirajStatus_nedovoljnoKapaciteta_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), eq(1L))).thenReturn(8);

            TerminZauzetException izuzetak = assertThrows(TerminZauzetException.class,
                    () -> servis.azurirajStatus(1L, StatusRezervacije.ODOBRENA));
            assertTrue(izuzetak.getMessage().contains("nema dovoljno slobodnog mesta"));
        }

        @Test
        void azurirajStatus_tihoPreskacIsteklu_neprekidaObraduOstalih() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije istekla = napraviStavku(1L, sala, 3, StatusStavke.ISTEKLA);
            StavkaRezervacije naCekanju = napraviStavku(2L, sala, 3, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, istekla, naCekanju);

            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(sale.findByIdForUpdate(1L)).thenReturn(Optional.of(sala));
            when(rezervacije.zauzetoOsobaUTerminu(eq(1L), any(), any(), any(), any())).thenReturn(0);
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            servis.azurirajStatus(1L, StatusRezervacije.ODOBRENA);

            assertEquals(StatusStavke.ISTEKLA, istekla.getStatusStavke());
            assertEquals(StatusStavke.ODOBRENA, naCekanju.getStatusStavke());
            assertEquals(StatusRezervacije.DELIMICNO_ODOBRENA, r.getStatus());
        }

        @Test
        void azurirajStatus_uspesnoOdbijanjeCeleRezervacije() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            servis.azurirajStatus(1L, StatusRezervacije.ODBIJENA);

            assertEquals(StatusStavke.ODBIJENA, st.getStatusStavke());
            assertEquals(StatusRezervacije.ODBIJENA, r.getStatus());
        }
    }

    @Nested
    class OtkaziRezervacijuTest {

        @Test
        void otkaziRezervaciju_vecOtkazana_bacaIzuzetak() {
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.OTKAZANA);
            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.otkaziRezervaciju(1L));

            assertEquals("Rezervacija je već otkazana.", izuzetak.getMessage());
        }

        @Test
        void otkaziRezervaciju_terminIstekao_oznacavaIsteklomIBacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().minusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.otkaziRezervaciju(1L));

            assertEquals("Rezervacija je istekla (datum termina je prošao) i ne može se otkazati.",
                    izuzetak.getMessage());
        }

        @Test
        void otkaziRezervaciju_uspesnoOtkazuje() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(rezervacije.findById(1L)).thenReturn(Optional.of(r));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            servis.otkaziRezervaciju(1L);

            assertEquals(StatusStavke.OTKAZANA, st.getStatusStavke());
            assertEquals(StatusRezervacije.OTKAZANA, r.getStatus());
        }
    }

    @Nested
    class OtkaziStavkuTest {

        @Test
        void otkaziStavku_stavkaNePostoji_bacaIzuzetak() {
            when(stavkeRepo.findById(1L)).thenReturn(Optional.empty());

            ResursNijePronadjenException izuzetak = assertThrows(ResursNijePronadjenException.class,
                    () -> servis.otkaziStavku(1L));
            assertEquals("Stavka rezervacije ne postoji.", izuzetak.getMessage());
        }

        @Test
        void otkaziStavku_terminIstekaoUMedjuvremenu_oznacavaIBacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            napraviRezervaciju(1L, LocalDate.now().minusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class,
                    () -> servis.otkaziStavku(1L));
            assertTrue(izuzetak.getMessage().contains("istekao"));
            assertEquals(StatusStavke.ISTEKLA, st.getStatusStavke());
        }

        @Test
        void otkaziStavku_vecOtkazana_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.OTKAZANA);
            napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.OTKAZANA, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class, () -> servis.otkaziStavku(1L));
            assertEquals("Stavka je već otkazana.", izuzetak.getMessage());
        }

        @Test
        void otkaziStavku_vecOdbijena_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.ODBIJENA);
            napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.ODBIJENA, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class, () -> servis.otkaziStavku(1L));
            assertEquals("Stavka je već odbijena i ne može se otkazati.", izuzetak.getMessage());
        }

        @Test
        void otkaziStavku_vecIstekla_bacaIzuzetak() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.ISTEKLA);
            napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.ISTEKLA, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);

            StatusTranzicijaNijeDozvoljenaException izuzetak = assertThrows(
                    StatusTranzicijaNijeDozvoljenaException.class, () -> servis.otkaziStavku(1L));
            assertEquals("Stavka je istekla (datum termina je prošao) i ne može se otkazati.",
                    izuzetak.getMessage());
        }

        @Test
        void otkaziStavku_uspesnoOtkazuje() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().plusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(stavkeRepo.findById(1L)).thenReturn(Optional.of(st));
            when(korisnikService.trenutniKorisnik()).thenReturn(obicanKorisnik);
            when(mapper.toDto(r)).thenReturn(new RezervacijaDto());

            servis.otkaziStavku(1L);

            assertEquals(StatusStavke.OTKAZANA, st.getStatusStavke());
        }
    }

    @Nested
    class OznaciIstekleStavkeTest {

        @Test
        void oznaciIstekleStavke_oznacavaPronadjeneIPreracunavaStatus() {
            Sala sala = napraviSalu(1L, "Sala 1", 10, StatusSale.SLOBODNA);
            StavkaRezervacije st = napraviStavku(1L, sala, 5, StatusStavke.NA_CEKANJU);
            Rezervacija r = napraviRezervaciju(1L, LocalDate.now().minusDays(1), obicanKorisnik,
                    StatusRezervacije.NA_CEKANJU, st);

            when(stavkeRepo.findIstekleNaCekanju(any())).thenReturn(List.of(st));

            servis.oznaciIstekleStavke();

            assertEquals(StatusStavke.ISTEKLA, st.getStatusStavke());
            assertEquals(StatusRezervacije.ISTEKLA, r.getStatus());
        }

        @Test
        void oznaciIstekleStavke_nemaIsteklih_nistaNeMenja() {
            when(stavkeRepo.findIstekleNaCekanju(any())).thenReturn(List.of());

            assertDoesNotThrow(() -> servis.oznaciIstekleStavke());
        }
    }
}
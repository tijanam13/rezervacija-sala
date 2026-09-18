package com.fon.rezervacija_sala.mapper.impl;

import com.fon.rezervacija_sala.dto.*;
import com.fon.rezervacija_sala.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SvrhaRezervacijeMapperTest {

    @Mock
    private PredavacMapper predavacMapper;
    @Mock
    private UcesnikSastankaMapper ucesnikMapper;

    private SvrhaRezervacijeMapper mapper;

    @BeforeEach
    void priprema() {
        mapper = new SvrhaRezervacijeMapper(predavacMapper, ucesnikMapper);
    }

    @Nested
    class ToDtoTest {

        @Test
        void toDto_null_vracaNull() {
            assertNull(mapper.toDto(null));
        }

        @Test
        void toDto_nastava_ispravnoMapira() {
            Nastava n = new Nastava();
            n.setId(1L);
            n.setSemestar(3);
            n.setNivoStudija(NivoStudija.OSNOVNE_AKADEMSKE);
            n.setVrsta(VrstaNastave.VEZBE);
            n.setVrstaVezbi(VrstaVezbi.RACUNSKE);

            SvrhaRezervacijeDto rezultat = mapper.toDto(n);

            assertInstanceOf(NastavaDto.class, rezultat);
            NastavaDto dto = (NastavaDto) rezultat;
            assertEquals(1L, dto.getId());
            assertEquals(3, dto.getSemestar());
            assertEquals(VrstaNastave.VEZBE, dto.getVrsta());
            assertEquals(VrstaVezbi.RACUNSKE, dto.getVrstaVezbi());
        }

        @Test
        void toDto_ispit_ispravnoMapira() {
            Ispit i = new Ispit();
            i.setId(2L);
            i.setSemestar(5);
            i.setNivoStudija(NivoStudija.MASTER);
            i.setTip(TipIspita.PISMENI);

            SvrhaRezervacijeDto rezultat = mapper.toDto(i);

            assertInstanceOf(IspitDto.class, rezultat);
            assertEquals(TipIspita.PISMENI, ((IspitDto) rezultat).getTipIspita());
        }

        @Test
        void toDto_zavrsniRad_ispravnoMapiraUkljucujuciMentoraIKomisiju() {
            ZavrsniRad z = new ZavrsniRad();
            z.setId(3L);
            z.setNazivTeme("Sistem za rezervaciju sala");
            z.setStudent("Marko Marković");

            Predavac mentor = new Predavac();
            mentor.setId(10L);
            z.setMentor(mentor);

            when(predavacMapper.toDto(mentor)).thenReturn(new PredavacDto());
            when(predavacMapper.toDtoList(any())).thenReturn(List.of());

            SvrhaRezervacijeDto rezultat = mapper.toDto(z);

            assertInstanceOf(ZavrsniRadDto.class, rezultat);
            ZavrsniRadDto dto = (ZavrsniRadDto) rezultat;
            assertEquals("Sistem za rezervaciju sala", dto.getNazivTeme());
            assertEquals("Marko Marković", dto.getStudent());
            assertNotNull(dto.getMentor());
        }

        @Test
        void toDto_sastanak_ispravnoMapiraUkljucujuciUcesnike() {
            Sastanak s = new Sastanak(4L, "Sastanak katedre", "Napomena");
            when(ucesnikMapper.toDtoList(any())).thenReturn(List.of());

            SvrhaRezervacijeDto rezultat = mapper.toDto(s);

            assertInstanceOf(SastanakDto.class, rezultat);
            SastanakDto dto = (SastanakDto) rezultat;
            assertEquals("Sastanak katedre", dto.getTema());
            assertEquals("Napomena", dto.getNapomena());
        }

        @Test
        void toDto_dogadjaj_ispravnoMapira() {
            Dogadjaj d = new Dogadjaj(5L, "Gostujuće predavanje", "Opis događaja");

            SvrhaRezervacijeDto rezultat = mapper.toDto(d);

            assertInstanceOf(DogadjajDto.class, rezultat);
            DogadjajDto dto = (DogadjajDto) rezultat;
            assertEquals("Gostujuće predavanje", dto.getNaziv());
            assertEquals("Opis događaja", dto.getOpis());
        }

        @Test
        void toDto_nepoznatPodtip_bacaIzuzetak() {
            SvrhaRezervacije nepoznat = new SvrhaRezervacije() {
            };

            IllegalStateException izuzetak = assertThrows(IllegalStateException.class,
                    () -> mapper.toDto(nepoznat));
            assertTrue(izuzetak.getMessage().contains("Nepoznata podvrsta"));
        }
    }

    @Nested
    class ToEntityTest {

        @Test
        void toEntity_null_vracaNull() {
            assertNull(mapper.toEntity(null));
        }

        @Test
        void toEntity_nastavaDto_ispravnoMapira() {
            NastavaDto dto = new NastavaDto();
            dto.setSemestar(2);
            dto.setNivoStudija(NivoStudija.OSNOVNE_AKADEMSKE);
            dto.setVrsta(VrstaNastave.PREDAVANJE);

            SvrhaRezervacije rezultat = mapper.toEntity(dto);

            assertInstanceOf(Nastava.class, rezultat);
            assertEquals(VrstaNastave.PREDAVANJE, ((Nastava) rezultat).getVrsta());
        }

        @Test
        void toEntity_ispitDto_ispravnoMapira() {
            IspitDto dto = new IspitDto();
            dto.setTipIspita(TipIspita.USMENI);

            SvrhaRezervacije rezultat = mapper.toEntity(dto);

            assertInstanceOf(Ispit.class, rezultat);
            assertEquals(TipIspita.USMENI, ((Ispit) rezultat).getTip());
        }

        @Test
        void toEntity_zavrsniRadDtoSaMentorom_ispravnoPovezujeMentora() {
            PredavacDto mentorDto = new PredavacDto();
            mentorDto.setId(10L);

            ZavrsniRadDto dto = new ZavrsniRadDto();
            dto.setNazivTeme("Tema");
            dto.setMentor(mentorDto);

            SvrhaRezervacije rezultat = mapper.toEntity(dto);

            assertInstanceOf(ZavrsniRad.class, rezultat);
            ZavrsniRad z = (ZavrsniRad) rezultat;
            assertNotNull(z.getMentor());
            assertEquals(10L, z.getMentor().getId());
        }

        @Test
        void toEntity_zavrsniRadDtoBezMentora_mentorOstajeNull() {
            ZavrsniRadDto dto = new ZavrsniRadDto();
            dto.setNazivTeme("Tema");
            dto.setMentor(null);

            SvrhaRezervacije rezultat = mapper.toEntity(dto);

            assertNull(((ZavrsniRad) rezultat).getMentor());
        }

        @Test
        void toEntity_sastanakDto_ispravnoMapiraUcesnike() {
            SastanakDto dto = new SastanakDto();
            dto.setTema("Sastanak");
            when(ucesnikMapper.toEntityList(any())).thenReturn(List.of());

            SvrhaRezervacije rezultat = mapper.toEntity(dto);

            assertInstanceOf(Sastanak.class, rezultat);
            assertEquals("Sastanak", ((Sastanak) rezultat).getTema());
        }

        @Test
        void toEntity_dogadjajDto_ispravnoMapira() {
            DogadjajDto dto = new DogadjajDto();
            dto.setNaziv("Događaj");
            dto.setOpis("Opis");

            SvrhaRezervacije rezultat = mapper.toEntity(dto);

            assertInstanceOf(Dogadjaj.class, rezultat);
            assertEquals("Događaj", ((Dogadjaj) rezultat).getNaziv());
        }

        @Test
        void toEntity_nepoznatPodtip_bacaIzuzetak() {
            SvrhaRezervacijeDto nepoznat = new SvrhaRezervacijeDto() {
            };

            IllegalStateException izuzetak = assertThrows(IllegalStateException.class,
                    () -> mapper.toEntity(nepoznat));
            assertTrue(izuzetak.getMessage().contains("Nepoznata podvrsta"));
        }
    }
}
package com.fon.rezervacija_sala.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;


class KatedraDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void priprema() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void ciscenje() {
        factory.close();
    }

    @Test
    void konstruktorSaSvimPoljima_ispravnoIhPostavlja() {
        KatedraDto dto = new KatedraDto(1L, "Katedra za softversko inženjerstvo", "Opis katedre");

        assertEquals(1L, dto.getId());
        assertEquals("Katedra za softversko inženjerstvo", dto.getNaziv());
        assertEquals("Opis katedre", dto.getOpis());
    }

    @Test
    void praznKonstruktorPaSeteri_ispravnoPostavljajuPolja() {
        KatedraDto dto = new KatedraDto();

        dto.setId(2L);
        dto.setNaziv("Katedra za informacione sisteme");
        dto.setOpis("Neki opis");

        assertEquals(2L, dto.getId());
        assertEquals("Katedra za informacione sisteme", dto.getNaziv());
        assertEquals("Neki opis", dto.getOpis());
    }

    @Test
    void nazivPrazan_javljaGreskuValidacije() {
        KatedraDto dto = new KatedraDto(null, "", "opis");

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertFalse(greske.isEmpty());
        assertTrue(greske.stream()
                .anyMatch(g -> g.getMessage().equals("Naziv katedre je obavezan.")));
    }

    @Test
    void nazivNull_javljaGreskuValidacije() {
        KatedraDto dto = new KatedraDto(null, null, "opis");

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertTrue(greske.stream()
                .anyMatch(g -> g.getMessage().equals("Naziv katedre je obavezan.")));
    }

    @Test
    void nazivKraciOd2Karaktera_javljaGreskuValidacije() {
        KatedraDto dto = new KatedraDto(null, "K", "opis");

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertFalse(greske.isEmpty());
    }

    @Test
    void nazivPredugacak_javljaGreskuValidacije() {
        String predugacakNaziv = "a".repeat(150); 
        KatedraDto dto = new KatedraDto(null, predugacakNaziv, "opis");

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertFalse(greske.isEmpty());
    }

    @ParameterizedTest(name = "dužina naziva={0} karaktera")
    @ValueSource(ints = {1, 2, 100, 101})
    void nazivGraniceDuzine(int duzina) {
        String naziv = "a".repeat(duzina);
        KatedraDto dto = new KatedraDto(null, naziv, "opis");

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        boolean uOkviruGranica = duzina >= 2 && duzina <= 100;
        assertEquals(uOkviruGranica, greske.isEmpty(),
                "Za dužinu " + duzina + " očekivano ispravno = " + uOkviruGranica);
    }

    @Test
    void opisPredugacak_javljaGreskuValidacije() {
        String predugacakOpis = "a".repeat(600); 
        KatedraDto dto = new KatedraDto(null, "Katedra za softversko inženjerstvo", predugacakOpis);

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertFalse(greske.isEmpty());
    }

    @Test
    void opisNull_nemaGresaka() {
        KatedraDto dto = new KatedraDto(null, "Katedra za softversko inženjerstvo", null);

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertTrue(greske.isEmpty());
    }

    @Test
    void ispravniPodaci_nemaGresaka() {
        KatedraDto dto = new KatedraDto(null, "Katedra za softversko inženjerstvo", "Opis katedre");

        Set<ConstraintViolation<KatedraDto>> greske = validator.validate(dto);

        assertTrue(greske.isEmpty());
    }
}
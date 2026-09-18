package com.fon.rezervacija_sala.repository.impl;

import com.fon.rezervacija_sala.entity.Katedra;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.context.annotation.Import; 

@DataJpaTest
@Import(KatedraRepository.class)
@ActiveProfiles("test")
class KatedraRepositoryTest {

    @Autowired
    private KatedraRepository katedraRepository;

    @Test
    void findAll_praznaBaza_vracaPraznuListu() {
        assertTrue(katedraRepository.findAll().isEmpty());
    }

    @Test
    void findAll_naknadnoDodataKatedra_vracaJe() {
        Katedra k = new Katedra();
        k.setNaziv("Katedra za softversko inženjerstvo");
        katedraRepository.save(k);

        List<Katedra> sve = katedraRepository.findAll();

        assertEquals(1, sve.size());
    }

    @Test
    void findById_postojecaKatedra_pronalaziJe() {
        Katedra k = new Katedra();
        k.setNaziv("Katedra za informacione sisteme");
        katedraRepository.save(k);

        Optional<Katedra> pronadjena = katedraRepository.findById(k.getId());

        assertTrue(pronadjena.isPresent());
        assertEquals("Katedra za informacione sisteme", pronadjena.get().getNaziv());
    }

    @Test
    void findById_nepostojecaKatedra_vracaPrazanOptional() {
        assertTrue(katedraRepository.findById(999L).isEmpty());
    }

    @Test
    void save_novaKatedra_dobijaGenerisaniId() {
        Katedra k = new Katedra();
        k.setNaziv("Nova katedra");

        katedraRepository.save(k);

        assertNotNull(k.getId());
    }

    @Test
    void save_izmenaPostojece_azurirajePodatke() {
        Katedra k = new Katedra();
        k.setNaziv("Stari naziv");
        katedraRepository.save(k);

        k.setNaziv("Novi naziv");
        katedraRepository.save(k);

        Optional<Katedra> ucitana = katedraRepository.findById(k.getId());
        assertEquals("Novi naziv", ucitana.get().getNaziv());
    }

    @Test
    void deleteById_postojecaKatedra_brisejeIzBaze() {
        Katedra k = new Katedra();
        k.setNaziv("Za brisanje");
        katedraRepository.save(k);
        Long id = k.getId();

        katedraRepository.deleteById(id);

        assertTrue(katedraRepository.findById(id).isEmpty());
    }

    @Test
    void deleteById_nepostojecId_neBacaIzuzetak() {
        assertDoesNotThrow(() -> katedraRepository.deleteById(999L));
    }

    @Test
    void findByNaziv_postojecaKatedra_pronalaziJe() {
        Katedra k = new Katedra();
        k.setNaziv("Katedra za softversko inženjerstvo");
        katedraRepository.save(k);

        Optional<Katedra> pronadjena = katedraRepository.findByNaziv("Katedra za softversko inženjerstvo");

        assertTrue(pronadjena.isPresent());
    }

    @Test
    void findByNaziv_nepostojecaKatedra_vracaPrazanOptional() {
        assertTrue(katedraRepository.findByNaziv("Katedra koja ne postoji").isEmpty());
    }
}
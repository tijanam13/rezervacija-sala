package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.RezervacijaDto;
import com.fon.rezervacija_sala.entity.StatusRezervacije;
import com.fon.rezervacija_sala.entity.StatusStavke;
import com.fon.rezervacija_sala.service.RezervacijaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rezervacija")
public class RezervacijaController {

    private final RezervacijaService rezervacijaService;

    public RezervacijaController(RezervacijaService rezervacijaService) {
        this.rezervacijaService = rezervacijaService;
    }

    @GetMapping
    public List<RezervacijaDto> findAll(@RequestParam(required = false) StatusRezervacije status) {
        return status != null ? rezervacijaService.findByStatus(status) : rezervacijaService.findAll();
    }

    @GetMapping("/{id}")
    public RezervacijaDto findById(@PathVariable Long id) {
        return rezervacijaService.findById(id);
    }

    @GetMapping("/moje-rezervacije")
    public List<RezervacijaDto> mojeRezervacije() {
        return rezervacijaService.mojeRezervacije();
    }

    @GetMapping("/korisnik/{korisnikId}")
    public List<RezervacijaDto> findByKorisnikId(@PathVariable Long korisnikId) {
        return rezervacijaService.findByKorisnikId(korisnikId);
    }

    @PostMapping
    public ResponseEntity<RezervacijaDto> create(@Valid @RequestBody RezervacijaDto dto) {
        RezervacijaDto kreirana = rezervacijaService.create(dto);
        return ResponseEntity.status(201).body(kreirana);
    }

    @PatchMapping("/{id}/status")
    public RezervacijaDto azurirajStatus(@PathVariable Long id, @RequestBody StatusRezervacije noviStatus) {
        return rezervacijaService.azurirajStatus(id, noviStatus);
    }

    @PatchMapping("/stavka/{stavkaId}/status")
    public RezervacijaDto azurirajStatusStavke(@PathVariable Long stavkaId, @RequestBody StatusStavke noviStatus) {
        return rezervacijaService.azurirajStatusStavke(stavkaId, noviStatus);
    }

    @PatchMapping("/{id}/otkazi")
    public RezervacijaDto otkaziRezervaciju(@PathVariable Long id) {
        return rezervacijaService.otkaziRezervaciju(id);
    }

    @PatchMapping("/stavka/{stavkaId}/otkazi")
    public RezervacijaDto otkaziStavku(@PathVariable Long stavkaId) {
        return rezervacijaService.otkaziStavku(stavkaId);
    }

}
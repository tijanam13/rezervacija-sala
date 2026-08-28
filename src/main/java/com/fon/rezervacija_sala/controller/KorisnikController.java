package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.KorisnikDto;
import com.fon.rezervacija_sala.entity.NazivUloge;
import com.fon.rezervacija_sala.entity.StatusNaloga;
import com.fon.rezervacija_sala.service.KorisnikService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/korisnik")
public class KorisnikController {

    private final KorisnikService korisnikService;

    public KorisnikController(KorisnikService korisnikService) {
        this.korisnikService = korisnikService;
    }

    @GetMapping
    public List<KorisnikDto> findAll() {
        return korisnikService.findAll();
    }

    @GetMapping("/{id}")
    public KorisnikDto findById(@PathVariable Long id) {
        return korisnikService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public KorisnikDto promeniStatus(@PathVariable Long id, @RequestBody StatusNaloga noviStatus) {
        return korisnikService.promeniStatus(id, noviStatus);
    }

    @PostMapping("/{id}/uloga/{naziv}")
    public KorisnikDto dodeliUlogu(@PathVariable Long id, @PathVariable NazivUloge naziv) {
        return korisnikService.dodeliUlogu(id, naziv);
    }

    @DeleteMapping("/{id}/uloga/{naziv}")
    public KorisnikDto oduzmiUlogu(@PathVariable Long id, @PathVariable NazivUloge naziv) {
        return korisnikService.oduzmiUlogu(id, naziv);
    }

}
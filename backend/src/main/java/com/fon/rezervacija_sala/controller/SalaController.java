package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.SalaDto;
import com.fon.rezervacija_sala.entity.StatusSale;
import com.fon.rezervacija_sala.service.SalaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sala")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public List<SalaDto> findAll() {
        return salaService.findAll();
    }

    @PostMapping
    public ResponseEntity<SalaDto> create(@Valid @RequestBody SalaDto dto) {
        SalaDto kreirana = salaService.create(dto);
        return ResponseEntity.status(201).body(kreirana);
    }

    @PutMapping("/{id}")
    public SalaDto update(@PathVariable Long id, @Valid @RequestBody SalaDto dto) {
        return salaService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public SalaDto promeniStatus(@PathVariable Long id, @RequestBody StatusSale noviStatus) {
        return salaService.promeniStatus(id, noviStatus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        salaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.KatedraDto;
import com.fon.rezervacija_sala.service.KatedraService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/katedra")
public class KatedraController {

    private final KatedraService katedraService;

    public KatedraController(KatedraService katedraService) {
        this.katedraService = katedraService;
    }

    @GetMapping
    public List<KatedraDto> findAll() {
        return katedraService.findAll();
    }

    @PostMapping
    public ResponseEntity<KatedraDto> create(@Valid @RequestBody KatedraDto dto) {
        KatedraDto kreirana = katedraService.create(dto);
        return ResponseEntity.status(201).body(kreirana);
    }

    @PutMapping("/{id}")
    public KatedraDto update(@PathVariable Long id, @Valid @RequestBody KatedraDto dto) {
        return katedraService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        katedraService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
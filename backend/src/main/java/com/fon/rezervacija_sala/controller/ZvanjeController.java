package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.ZvanjeDto;
import com.fon.rezervacija_sala.service.ZvanjeService;
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
@RequestMapping("/api/zvanje")
public class ZvanjeController {

    private final ZvanjeService zvanjeService;

    public ZvanjeController(ZvanjeService zvanjeService) {
        this.zvanjeService = zvanjeService;
    }

    @GetMapping
    public List<ZvanjeDto> findAll() {
        return zvanjeService.findAll();
    }

    @GetMapping("/{id}")
    public ZvanjeDto findById(@PathVariable Long id) {
        return zvanjeService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ZvanjeDto> create(@Valid @RequestBody ZvanjeDto dto) {
        ZvanjeDto kreirano = zvanjeService.create(dto);
        return ResponseEntity.status(201).body(kreirano);
    }

    @PutMapping("/{id}")
    public ZvanjeDto update(@PathVariable Long id, @Valid @RequestBody ZvanjeDto dto) {
        return zvanjeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        zvanjeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
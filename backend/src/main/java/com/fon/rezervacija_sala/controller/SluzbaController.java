package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.SluzbaDto;
import com.fon.rezervacija_sala.service.SluzbaService;
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
@RequestMapping("/api/sluzba")
public class SluzbaController {

    private final SluzbaService sluzbaService;

    public SluzbaController(SluzbaService sluzbaService) {
        this.sluzbaService = sluzbaService;
    }

    @GetMapping
    public List<SluzbaDto> findAll() {
        return sluzbaService.findAll();
    }

    @PostMapping
    public ResponseEntity<SluzbaDto> create(@Valid @RequestBody SluzbaDto dto) {
        SluzbaDto kreirana = sluzbaService.create(dto);
        return ResponseEntity.status(201).body(kreirana);
    }

    @PutMapping("/{id}")
    public SluzbaDto update(@PathVariable Long id, @Valid @RequestBody SluzbaDto dto) {
        return sluzbaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        sluzbaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
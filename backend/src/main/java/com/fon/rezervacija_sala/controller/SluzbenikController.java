package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.SluzbenikDto;
import com.fon.rezervacija_sala.service.SluzbenikService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sluzbenik")
public class SluzbenikController {

    private final SluzbenikService sluzbenikService;

    public SluzbenikController(SluzbenikService sluzbenikService) {
        this.sluzbenikService = sluzbenikService;
    }

    @GetMapping
    public List<SluzbenikDto> findAll() {
        return sluzbenikService.findAll();
    }

    @GetMapping("/{id}")
    public SluzbenikDto findById(@PathVariable Long id) {
        return sluzbenikService.findById(id);
    }

    @GetMapping("/sluzba/{sluzbaId}")
    public List<SluzbenikDto> findBySluzba(@PathVariable Long sluzbaId) {
        return sluzbenikService.findBySluzba(sluzbaId);
    }

    @PutMapping("/{id}")
    public SluzbenikDto update(@PathVariable Long id, @Valid @RequestBody SluzbenikDto dto) {
        return sluzbenikService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        sluzbenikService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
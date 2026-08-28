package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.PredavacDto;
import com.fon.rezervacija_sala.service.PredavacService;
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
@RequestMapping("/api/predavac")
public class PredavacController {

    private final PredavacService predavacService;

    public PredavacController(PredavacService predavacService) {
        this.predavacService = predavacService;
    }

    @GetMapping
    public List<PredavacDto> findAll() {
        return predavacService.findAll();
    }

    @GetMapping("/{id}")
    public PredavacDto findById(@PathVariable Long id) {
        return predavacService.findById(id);
    }

    @GetMapping("/katedra/{katedraId}")
    public List<PredavacDto> findByKatedra(@PathVariable Long katedraId) {
        return predavacService.findByKatedra(katedraId);
    }

    @PutMapping("/{id}")
    public PredavacDto update(@PathVariable Long id, @Valid @RequestBody PredavacDto dto) {
        return predavacService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        predavacService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
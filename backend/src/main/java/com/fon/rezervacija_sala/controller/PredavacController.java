package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.PredavacDto;
import com.fon.rezervacija_sala.service.PredavacService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PredavacDto create(@Valid @RequestBody PredavacDto dto) {
        return predavacService.create(dto);
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
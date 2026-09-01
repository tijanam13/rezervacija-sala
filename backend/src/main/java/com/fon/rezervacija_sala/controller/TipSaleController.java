package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.TipSaleDto;
import com.fon.rezervacija_sala.service.TipSaleService;
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
@RequestMapping("/api/tip-sale")
public class TipSaleController {

    private final TipSaleService tipSaleService;

    public TipSaleController(TipSaleService tipSaleService) {
        this.tipSaleService = tipSaleService;
    }

    @GetMapping
    public List<TipSaleDto> findAll() {
        return tipSaleService.findAll();
    }

    @PostMapping
    public ResponseEntity<TipSaleDto> create(@Valid @RequestBody TipSaleDto dto) {
        TipSaleDto kreiran = tipSaleService.create(dto);
        return ResponseEntity.status(201).body(kreiran);
    }

    @PutMapping("/{id}")
    public TipSaleDto update(@PathVariable Long id, @Valid @RequestBody TipSaleDto dto) {
        return tipSaleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        tipSaleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
package com.fon.rezervacija_sala.controller;

import com.fon.rezervacija_sala.dto.AuthResponse;
import com.fon.rezervacija_sala.dto.KorisnikDto;
import com.fon.rezervacija_sala.dto.LoginRequest;
import com.fon.rezervacija_sala.dto.RegisterRequest;
import com.fon.rezervacija_sala.service.AuthService;
import com.fon.rezervacija_sala.service.KorisnikService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final KorisnikService korisnikService;

    public AuthController(AuthService authService, KorisnikService korisnikService) {
        this.authService = authService;
        this.korisnikService = korisnikService;
    }

    @GetMapping("/me")
    public KorisnikDto me() {
        return korisnikService.mojProfil();
    }

    @SecurityRequirements
    @PostMapping("/registracija")
    public ResponseEntity<KorisnikDto> registerKorisnik(@Valid @RequestBody RegisterRequest req) {
        KorisnikDto kreiran = authService.registerKorisnik(req);
        return ResponseEntity.status(201).body(kreiran);
    }

    @SecurityRequirements
    @PostMapping("/verifikuj-email")
    public AuthResponse verifyEmail(@RequestParam String token) {
        return authService.verifyEmail(token);
    }

    @SecurityRequirements
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @SecurityRequirements
    @PostMapping("/lozinka/zaboravljena")
    public ResponseEntity<Void> requestPasswordReset(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @SecurityRequirements
    @PostMapping("/lozinka/reset")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String novaLozinka) {
        authService.resetPassword(token, novaLozinka);
        return ResponseEntity.ok().build();
    }

}
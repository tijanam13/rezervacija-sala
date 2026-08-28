package com.fon.rezervacija_sala.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacija(MethodArgumentNotValidException ex) {
        Map<String, String> greskePoPolju = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            greskePoPolju.put(fe.getField(), fe.getDefaultMessage());
        }
        return odgovor(HttpStatus.BAD_REQUEST, "Neispravni podaci u zahtevu.", greskePoPolju);
    }

    @ExceptionHandler(PoslovnaGreskaException.class)
    public ResponseEntity<Map<String, Object>> handlePoslovnaGreska(PoslovnaGreskaException ex) {
        return odgovor(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return odgovor(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(ResursNijePronadjenException.class)
    public ResponseEntity<Map<String, Object>> handleResursNijePronadjen(ResursNijePronadjenException ex) {
        return odgovor(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(NevalidanZahtevException.class)
    public ResponseEntity<Map<String, Object>> handleNevalidanZahtev(NevalidanZahtevException ex) {
        return odgovor(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(PristupOdbijenException.class)
    public ResponseEntity<Map<String, Object>> handlePristupOdbijen(PristupOdbijenException ex) {
        log.warn("Pristup odbijen: {}", ex.getMessage());
        return odgovor(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        log.warn("Neuspešna autentikacija: {}", ex.getMessage());
        return odgovor(HttpStatus.UNAUTHORIZED, "Pogrešan email ili lozinka.", null);
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleZakljucavanje(PessimisticLockingFailureException ex) {
        log.warn("Konflikt pri istovremenom pristupu (zaključavanje reda u bazi): {}", ex.getMessage());
        return odgovor(HttpStatus.CONFLICT,
                "Došlo je do konflikta pri istovremenom pristupu istoj sali. Pokušajte ponovo.", null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimistickoZakljucavanje(OptimisticLockingFailureException ex) {
        log.warn("Konflikt pri istovremenoj izmeni (podatak je već izmenjen u međuvremenu): {}", ex.getMessage());
        return odgovor(HttpStatus.CONFLICT,
                "Ovaj podatak je u međuvremenu izmenjen od strane drugog korisnika. Pokušajte ponovo.",
                null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleNarusenoOgranicenje(DataIntegrityViolationException ex) {
        log.warn("Narušeno ograničenje baze podataka (npr. dupla vrednost): {}", ex.getMessage());
        return odgovor(HttpStatus.CONFLICT,
                "Podatak koji pokušavate da sačuvate se sukobljava sa postojećim (npr. već je u upotrebi). "
                + "Pokušajte ponovo.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOstalo(Exception ex) {
        log.error("Neočekivana greška prilikom obrade zahteva", ex);
        return odgovor(HttpStatus.INTERNAL_SERVER_ERROR,
                "Došlo je do neočekivane greške. Pokušajte ponovo kasnije.", null);
    }

    private ResponseEntity<Map<String, Object>> odgovor(HttpStatus status, String poruka, Map<String, String> greske) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("poruka", poruka);
        if (greske != null && !greske.isEmpty()) {
            body.put("greske", greske);
        }
        return ResponseEntity.status(status).body(body);
    }

}
package com.fon.rezervacija_sala.exception;

public class NevalidanZahtevException extends RuntimeException {

    public NevalidanZahtevException(String poruka) {
        super(poruka);
    }

}

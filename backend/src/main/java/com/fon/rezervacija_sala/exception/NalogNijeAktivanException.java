package com.fon.rezervacija_sala.exception;

public class NalogNijeAktivanException extends PoslovnaGreskaException {

    public NalogNijeAktivanException(String poruka) {
        super(poruka);
    }

}
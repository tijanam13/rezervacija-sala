package com.fon.rezervacija_sala.exception;

public class NalogZakljucanException extends PoslovnaGreskaException {

    public NalogZakljucanException(String poruka) {
        super(poruka);
    }

}
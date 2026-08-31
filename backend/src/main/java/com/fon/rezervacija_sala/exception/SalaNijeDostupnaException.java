package com.fon.rezervacija_sala.exception;

public class SalaNijeDostupnaException extends PoslovnaGreskaException {

    public SalaNijeDostupnaException(String poruka) {
        super(poruka);
    }

}

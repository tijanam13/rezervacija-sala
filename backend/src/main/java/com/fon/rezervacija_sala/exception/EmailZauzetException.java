package com.fon.rezervacija_sala.exception;

public class EmailZauzetException extends PoslovnaGreskaException {

    public EmailZauzetException(String poruka) {
        super(poruka);
    }

}
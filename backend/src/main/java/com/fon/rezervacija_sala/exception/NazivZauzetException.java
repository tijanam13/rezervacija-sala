package com.fon.rezervacija_sala.exception;

public class NazivZauzetException extends PoslovnaGreskaException {

    public NazivZauzetException(String poruka) {
        super(poruka);
    }

}
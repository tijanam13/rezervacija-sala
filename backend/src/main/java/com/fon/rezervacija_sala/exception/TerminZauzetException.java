package com.fon.rezervacija_sala.exception;

public class TerminZauzetException extends PoslovnaGreskaException {

    public TerminZauzetException(String poruka) {
        super(poruka);
    }

}

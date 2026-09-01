package com.fon.rezervacija_sala.exception;

public class BrisanjeNijeMoguceException extends PoslovnaGreskaException {

    public BrisanjeNijeMoguceException(String poruka) {
        super(poruka);
    }

}
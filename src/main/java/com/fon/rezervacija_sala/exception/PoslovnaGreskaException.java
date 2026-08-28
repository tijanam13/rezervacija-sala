package com.fon.rezervacija_sala.exception;

public abstract class PoslovnaGreskaException extends RuntimeException {

    protected PoslovnaGreskaException(String poruka) {
        super(poruka);
    }

}
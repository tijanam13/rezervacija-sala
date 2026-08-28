package com.fon.rezervacija_sala.exception;

public class TokenNevalidanException extends PoslovnaGreskaException {

    public TokenNevalidanException(String poruka) {
        super(poruka);
    }

}
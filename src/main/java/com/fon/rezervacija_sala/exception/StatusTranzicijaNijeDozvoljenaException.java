package com.fon.rezervacija_sala.exception;

public class StatusTranzicijaNijeDozvoljenaException extends PoslovnaGreskaException {

    public StatusTranzicijaNijeDozvoljenaException(String poruka) {
        super(poruka);
    }

}

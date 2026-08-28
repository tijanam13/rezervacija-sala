package com.fon.rezervacija_sala.exception;

public class ResursNijePronadjenException extends RuntimeException {

    public ResursNijePronadjenException(String poruka) {
        super(poruka);
    }

}
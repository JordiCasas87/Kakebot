package com.jordi.kakebot.user.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales invalidas");
    }
}

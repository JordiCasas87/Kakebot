package com.jordi.kakebot.user.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("El nombre de usuario ya existe: " + username);
    }
}

package com.jordi.kakebot.user.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("No existe usuario con id: " + userId);
    }
}

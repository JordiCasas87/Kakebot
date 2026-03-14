package com.jordi.kakebot.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidExpenseRequestException extends RuntimeException {

    public InvalidExpenseRequestException(String message) {
        super(message);
    }
}

package com.jordi.kakebot.expense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExpenseUserNotFoundException extends RuntimeException {

    public ExpenseUserNotFoundException(Long userId) {
        super("No existe usuario con id: " + userId);
    }
}

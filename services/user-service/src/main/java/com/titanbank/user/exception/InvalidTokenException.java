package com.titanbank.user.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends TitanBankException {

    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
    }
}
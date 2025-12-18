package com.titanbank.user.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends TitanBankException {

    public InvalidFileException(String message) {
        super(message, "INVALID_FILE", HttpStatus.BAD_REQUEST);
    }
}
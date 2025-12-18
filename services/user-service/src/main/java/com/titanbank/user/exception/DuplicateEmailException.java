package com.titanbank.user.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends TitanBankException {

    public DuplicateEmailException(String message) {
        super(message, "DUPLICATE_EMAIL", HttpStatus.CONFLICT);
    }
}
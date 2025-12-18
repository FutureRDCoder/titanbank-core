package com.titanbank.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TitanBankException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public TitanBankException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
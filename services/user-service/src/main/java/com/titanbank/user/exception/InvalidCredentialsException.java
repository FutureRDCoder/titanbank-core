package com.titanbank.user.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends TitanBankException {

  public InvalidCredentialsException(String message) {
    super(message, "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
  }
}
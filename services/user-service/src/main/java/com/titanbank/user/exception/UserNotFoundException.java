package com.titanbank.user.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends TitanBankException {

  public UserNotFoundException(String message) {
    super(message, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
  }
}
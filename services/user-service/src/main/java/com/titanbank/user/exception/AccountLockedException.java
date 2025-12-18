package com.titanbank.user.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends TitanBankException {

    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED", HttpStatus.LOCKED);
    }
}
package com.titanbank.account.domain.enums;

public enum AccountStatus {

    ACTIVE,
    FROZEN,
    CLOSED;

    public boolean allowsDebit() {
        return this == ACTIVE;
    }

    public boolean allowsCredit() {
        return this == ACTIVE || this == FROZEN;
    }

    public boolean isTerminal() {
        return this == CLOSED;
    }
}
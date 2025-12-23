package com.titanbank.account.service;

import com.titanbank.account.domain.entity.Account;
import com.titanbank.account.domain.enums.AccountType;
import com.titanbank.account.domain.enums.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    Account createAccount(UUID userId, AccountType accountType, Currency currency);

    Account getAccount(UUID accountId, UUID userId);

    List<Account> getAccountsForUser(UUID userId);

    void credit(UUID accountId, UUID userId, BigDecimal amount);

    void debit(UUID accountId, UUID userId, BigDecimal amount);

    void freezeAccount(UUID accountId, UUID userId);

    void activateAccount(UUID accountId, UUID userId);

    void closeAccount(UUID accountId, UUID userId);
}
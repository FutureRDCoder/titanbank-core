package com.titanbank.account.service.impl;

import com.titanbank.account.domain.entity.Account;
import com.titanbank.account.domain.enums.AccountStatus;
import com.titanbank.account.domain.enums.AccountType;
import com.titanbank.account.domain.enums.Currency;
import com.titanbank.account.repository.AccountRepository;
import com.titanbank.account.service.AccountService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(UUID userId, AccountType accountType, Currency currency) {
        Account account = Account.create(userId, accountType, currency);
        return accountRepository.save(account);
    }

    @Override
    public Account getAccount(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found or access denied")
                );
    }

    @Override
    public List<Account> getAccountsForUser(UUID userId) {
        return accountRepository.findAllByUserId(userId);
    }

    @Override
    public void credit(UUID accountId, UUID userId, BigDecimal amount) {
        Account account = getAccount(accountId, userId);
        account.credit(amount);
        accountRepository.save(account);
    }

    @Override
    public void debit(UUID accountId, UUID userId, BigDecimal amount) {
        Account account = getAccount(accountId, userId);
        account.debit(amount);
        accountRepository.save(account);
    }

    @Override
    public void freezeAccount(UUID accountId, UUID userId) {
        Account account = getAccount(accountId, userId);
        account.freeze();
        accountRepository.save(account);
    }

    @Override
    public void activateAccount(UUID accountId, UUID userId) {
        Account account = getAccount(accountId, userId);
        account.activate();
        accountRepository.save(account);
    }

    @Override
    public void closeAccount(UUID accountId, UUID userId) {
        Account account = getAccount(accountId, userId);
        account.close();
        accountRepository.save(account);
    }
}
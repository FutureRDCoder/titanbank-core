package com.titanbank.account.domain.entity;

import com.titanbank.account.domain.enums.AccountStatus;
import com.titanbank.account.domain.enums.AccountType;
import com.titanbank.account.domain.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Account() {
        // JPA
    }

    private Account(
            UUID userId,
            AccountType accountType,
            Currency currency
    ) {
        this.userId = userId;
        this.accountType = accountType;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    /* =========================
       Factory Method
       ========================= */

    public static Account create(
            UUID userId,
            AccountType accountType,
            Currency currency
    ) {
        return new Account(userId, accountType, currency);
    }

    /* =========================
       Domain Behavior
       ========================= */

    public void credit(BigDecimal amount) {
        assertAmountPositive(amount);

        if (!status.allowsCredit()) {
            throw new IllegalStateException(
                    "Credits are not allowed when account status is " + status
            );
        }

        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        assertAmountPositive(amount);

        if (!status.allowsDebit()) {
            throw new IllegalStateException(
                    "Debits are not allowed when account status is " + status
            );
        }

        BigDecimal newBalance = this.balance.subtract(amount);

        if (newBalance.signum() < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        this.balance = newBalance;
    }

    public void freeze() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot freeze a closed account");
        }
        this.status = AccountStatus.FROZEN;
    }

    public void activate() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot activate a closed account");
        }
        this.status = AccountStatus.ACTIVE;
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
    }

    /* =========================
       Invariant Guards
       ========================= */

    private void assertAmountPositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}
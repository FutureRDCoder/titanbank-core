package com.titanbank.account.repository;

import com.titanbank.account.domain.entity.Account;
import com.titanbank.account.domain.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIdAndUserId(UUID accountId, UUID userId);

    List<Account> findAllByUserId(UUID userId);

    boolean existsByUserIdAndStatus(UUID userId, AccountStatus status);
}
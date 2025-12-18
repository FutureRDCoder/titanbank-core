package com.titanbank.user.repository;

import com.titanbank.user.model.entity.User;
import com.titanbank.user.model.enums.KYCStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data automatically implements this based on method name!
    // Generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Spring Data automatically implements this
    // Generates: SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // Custom JPQL query - find active user by email
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    // Spring Data automatically implements this
    // Generates: SELECT * FROM users WHERE kyc_status = ?
    List<User> findByKycStatus(KYCStatus status);

    // Custom UPDATE query - must use @Modifying and @Transactional
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    // Pessimistic locking - locks the row during read
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByIdWithLock(@Param("userId") Long userId);

    // Additional useful queries
    List<User> findByIsActive(Boolean isActive);

    List<User> findByKycStatusOrderByCreatedAtDesc(KYCStatus status);
}
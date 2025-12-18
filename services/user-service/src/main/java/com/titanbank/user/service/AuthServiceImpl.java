package com.titanbank.user.service;

import com.titanbank.user.dto.request.LoginRequest;
import com.titanbank.user.dto.response.LoginResponse;
import com.titanbank.user.event.UserLoggedInEvent;
import com.titanbank.user.exception.AccountLockedException;
import com.titanbank.user.exception.InvalidCredentialsException;
import com.titanbank.user.exception.InvalidTokenException;
import com.titanbank.user.exception.UserNotFoundException;
import com.titanbank.user.model.entity.User;
import com.titanbank.user.repository.UserRepository;
import com.titanbank.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "Account is temporarily locked due to multiple failed login attempts. Try again later."
            );
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Increment failed attempts
            user.incrementFailedLoginAttempts();
            userRepository.save(user);

            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Successful login - reset failed attempts
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Store refresh token in Redis
        storeRefreshToken(user.getUserId(), refreshToken, request.getRememberMe());

        // Publish UserLoggedIn event
        publishLoginEvent(user);

        log.info("User logged in successfully: userId={}, email={}",
                user.getUserId(), user.getEmail());

        // Build and return response
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .userInfo(buildUserInfo(user))
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        // Validate refresh token from Redis
        String userIdStr = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        if (userIdStr == null) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Verify user is still active
        if (!user.getIsActive()) {
            throw new InvalidTokenException("User account is no longer active");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        log.info("Access token refreshed for user: {}", user.getEmail());

        // Return response with new access token (same refresh token)
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .userInfo(buildUserInfo(user))
                .build();
    }

    @Override
    public void logout(String accessToken) {
        log.info("Logging out user");

        try {
            // Extract userId from token
            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);

            // Find and delete refresh token from Redis
            String refreshToken = redisTemplate.opsForValue().get("user_refresh_token:" + userId);
            if (refreshToken != null) {
                redisTemplate.delete("refresh_token:" + refreshToken);
                redisTemplate.delete("user_refresh_token:" + userId);
            }

            // Blacklist access token (until it expires)
            Long ttl = jwtTokenProvider.getTokenTTL(accessToken);
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        "blacklisted_token:" + accessToken,
                        "true",
                        Duration.ofSeconds(ttl)
                );
            }

            log.info("User logged out successfully: userId={}", userId);

        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new InvalidTokenException("Invalid token");
        }
    }

    // Helper methods

    private void storeRefreshToken(Long userId, String refreshToken, Boolean rememberMe) {
        Duration expiry = Boolean.TRUE.equals(rememberMe) ?
                Duration.ofDays(30) : Duration.ofDays(7);

        // Store refresh token with userId as value
        redisTemplate.opsForValue().set(
                "refresh_token:" + refreshToken,
                userId.toString(),
                expiry
        );

        // Store reverse mapping for logout
        redisTemplate.opsForValue().set(
                "user_refresh_token:" + userId,
                refreshToken,
                expiry
        );
    }

    private void publishLoginEvent(User user) {
        UserLoggedInEvent event = UserLoggedInEvent.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("user-events", event);
    }

    private LoginResponse.UserInfoDTO buildUserInfo(User user) {
        return LoginResponse.UserInfoDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .kycStatus(user.getKycStatus())
                .build();
    }
}
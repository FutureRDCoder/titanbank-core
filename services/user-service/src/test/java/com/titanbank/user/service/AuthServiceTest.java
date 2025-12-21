package com.titanbank.user.service;

import com.titanbank.user.dto.request.LoginRequest;
import com.titanbank.user.dto.response.LoginResponse;
import com.titanbank.user.exception.InvalidCredentialsException;
import com.titanbank.user.model.entity.User;
import com.titanbank.user.model.enums.KYCStatus;
import com.titanbank.user.model.enums.UserRole;
import com.titanbank.user.repository.UserRepository;
import com.titanbank.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .kycStatus(KYCStatus.PENDING)
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();

        testUser.addRole(UserRole.USER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setRememberMe(false);

        // Mock Redis operations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testLogin_Success() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken())
                .thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration())
                .thenReturn(900L);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
        assertThat(response.getUserInfo().getUserId()).isEqualTo(1L);

        verify(userRepository).save(testUser);
        verify(kafkaTemplate).send(eq("user-events"), any());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        when(userRepository.findActiveUserByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword"))
                .thenReturn(false);

        loginRequest.setPassword("wrongpassword");

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(userRepository).save(testUser);
        assertThat(testUser.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        when(userRepository.findActiveUserByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        loginRequest.setEmail("nonexistent@example.com");

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
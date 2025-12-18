package com.titanbank.user.security;

import com.titanbank.user.model.entity.User;
import com.titanbank.user.model.enums.KYCStatus;
import com.titanbank.user.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=testSecretKeyForJwtTokenGenerationAndValidationPurposesOnly",
        "jwt.access-token-expiration=900",
        "jwt.refresh-token-expiration=604800"
})
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    private User testUser;

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
                .build();

        testUser.addRole(UserRole.USER);
        testUser.addRole(UserRole.TRADER);
    }

    @Test
    void testGenerateAccessToken() {
        // When
        String token = tokenProvider.generateAccessToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void testValidateToken() {
        // Given
        String token = tokenProvider.generateAccessToken(testUser);

        // When
        boolean isValid = tokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testGetUserIdFromToken() {
        // Given
        String token = tokenProvider.generateAccessToken(testUser);

        // When
        Long userId = tokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void testGetEmailFromToken() {
        // Given
        String token = tokenProvider.generateAccessToken(testUser);

        // When
        String email = tokenProvider.getEmailFromToken(token);

        // Then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void testGetRolesFromToken() {
        // Given
        String token = tokenProvider.generateAccessToken(testUser);

        // When
        var roles = tokenProvider.getRolesFromToken(token);

        // Then
        assertThat(roles).contains(UserRole.USER, UserRole.TRADER);
    }

    @Test
    void testInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }
}
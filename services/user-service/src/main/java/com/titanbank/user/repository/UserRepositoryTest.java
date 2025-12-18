package com.titanbank.user.repository;

import com.titanbank.user.model.entity.User;
import com.titanbank.user.model.enums.KYCStatus;
import com.titanbank.user.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // Uses H2 in-memory database for testing
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByEmail() {
        // Given: Create a test user
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword123")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .kycStatus(KYCStatus.PENDING)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user.addRole(UserRole.USER);

        // When: Save user
        User savedUser = userRepository.save(user);

        // Then: Find by email should return the user
        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testExistsByEmail() {
        // Given
        User user = User.builder()
                .email("exists@example.com")
                .passwordHash("hash")
                .firstName("Jane")
                .lastName("Smith")
                .kycStatus(KYCStatus.PENDING)
                .build();

        userRepository.save(user);

        // When/Then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    void testFindActiveUserByEmail() {
        // Given: Active user
        User activeUser = User.builder()
                .email("active@example.com")
                .passwordHash("hash")
                .firstName("Active")
                .lastName("User")
                .kycStatus(KYCStatus.PENDING)
                .isActive(true)
                .build();

        // Given: Inactive user
        User inactiveUser = User.builder()
                .email("inactive@example.com")
                .passwordHash("hash")
                .firstName("Inactive")
                .lastName("User")
                .kycStatus(KYCStatus.PENDING)
                .isActive(false)
                .build();

        userRepository.save(activeUser);
        userRepository.save(inactiveUser);

        // When/Then: Should find active user
        Optional<User> found = userRepository.findActiveUserByEmail("active@example.com");
        assertThat(found).isPresent();

        // When/Then: Should NOT find inactive user
        Optional<User> notFound = userRepository.findActiveUserByEmail("inactive@example.com");
        assertThat(notFound).isEmpty();
    }
}
package com.titanbank.user.service;

import com.titanbank.user.dto.request.UserProfileUpdateRequest;
import com.titanbank.user.dto.request.UserRegistrationRequest;
import com.titanbank.user.dto.response.UserProfileResponse;
import com.titanbank.user.dto.response.UserRegistrationResponse;
import com.titanbank.user.event.UserProfileUpdatedEvent;
import com.titanbank.user.event.UserRegisteredEvent;
import com.titanbank.user.exception.DuplicateEmailException;
import com.titanbank.user.exception.InvalidFileException;
import com.titanbank.user.exception.InvalidTokenException;
import com.titanbank.user.exception.UserNotFoundException;
import com.titanbank.user.model.entity.User;
import com.titanbank.user.model.entity.UserProfile;
import com.titanbank.user.model.enums.DocumentType;
import com.titanbank.user.model.enums.KYCStatus;
import com.titanbank.user.model.enums.UserRole;
import com.titanbank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        // Create user entity
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .kycStatus(KYCStatus.PENDING)
                .isActive(true)
                .isEmailVerified(false)
                .build();

        user.addRole(UserRole.USER);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getUserId());

        // Generate email verification token
        String verificationToken = generateVerificationToken(savedUser.getUserId());

        // Publish UserRegistered event to Kafka
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .verificationToken(verificationToken)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        kafkaTemplate.send("user-events", event);
        log.info("Published UserRegistered event for user: {}", savedUser.getUserId());

        return UserRegistrationResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .message("Registration successful. Please check your email for verification.")
                .build();
    }

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Fetching profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Update user fields if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update profile
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        updateProfileFields(profile, request);

        User updatedUser = userRepository.save(user);

        // Publish UserProfileUpdated event
        UserProfileUpdatedEvent event = UserProfileUpdatedEvent.builder()
                .userId(updatedUser.getUserId())
                .timestamp(java.time.LocalDateTime.now())
                .build();

        kafkaTemplate.send("user-events", event);

        log.info("Profile updated successfully for user: {}", userId);
        return mapToProfileResponse(updatedUser);
    }

    @Override
    @Transactional
    public void uploadKYCDocument(Long userId, MultipartFile file, DocumentType documentType) {
        log.info("Uploading KYC document for user: {}, type: {}", userId, documentType);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Validate file
        validateKYCDocument(file);

        // TODO: Upload to S3 (implement later)
        // For now, just log and update KYC status
        String documentUrl = "s3://titanbank-kyc/" + userId + "/" + file.getOriginalFilename();

        user.setKycStatus(KYCStatus.SUBMITTED);
        userRepository.save(user);

        log.info("KYC document uploaded successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        String userIdStr = redisTemplate.opsForValue().get("email_verification:" + token);
        if (userIdStr == null) {
            throw new InvalidTokenException("Invalid or expired verification token");
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.setIsEmailVerified(true);
        userRepository.save(user);

        // Delete token from Redis
        redisTemplate.delete("email_verification:" + token);

        log.info("Email verified successfully for user: {}", userId);
    }

    @Override
    public void initiatePasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();

        // Store in Redis (1 hour expiry)
        redisTemplate.opsForValue().set(
                "password_reset:" + resetToken,
                user.getUserId().toString(),
                Duration.ofHours(1)
        );

        // TODO: Send email with reset link (implement with Notification Service)
        log.info("Password reset token generated for user: {}", user.getUserId());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        String userIdStr = redisTemplate.opsForValue().get("password_reset:" + token);
        if (userIdStr == null) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token from Redis
        redisTemplate.delete("password_reset:" + token);

        log.info("Password reset successfully for user: {}", userId);
    }

    // Helper methods

    private String generateVerificationToken(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "email_verification:" + token,
                userId.toString(),
                Duration.ofHours(24)
        );
        return token;
    }

    private void validateKYCDocument(MultipartFile file) {
        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new InvalidFileException("File size exceeds 10MB limit");
        }

        // Validate file type
        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/png", "application/pdf");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new InvalidFileException("Invalid file type. Allowed: JPEG, PNG, PDF");
        }
    }

    private void updateProfileFields(UserProfile profile, UserProfileUpdateRequest request) {
        if (request.getAddressLine1() != null) {
            profile.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            profile.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getState() != null) {
            profile.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            profile.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getEmploymentStatus() != null) {
            profile.setEmploymentStatus(request.getEmploymentStatus());
        }
        if (request.getAnnualIncome() != null) {
            profile.setAnnualIncome(request.getAnnualIncome());
        }
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse.UserProfileDTO profileDTO = null;

        if (user.getProfile() != null) {
            profileDTO = UserProfileResponse.UserProfileDTO.builder()
                    .addressLine1(user.getProfile().getAddressLine1())
                    .addressLine2(user.getProfile().getAddressLine2())
                    .city(user.getProfile().getCity())
                    .state(user.getProfile().getState())
                    .postalCode(user.getProfile().getPostalCode())
                    .country(user.getProfile().getCountry())
                    .employmentStatus(user.getProfile().getEmploymentStatus())
                    .annualIncome(user.getProfile().getAnnualIncome())
                    .build();
        }

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .kycStatus(user.getKycStatus())
                .roles(user.getRoles())
                .isEmailVerified(user.getIsEmailVerified())
                .profile(profileDTO)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
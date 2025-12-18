package com.titanbank.user.service;

import com.titanbank.user.dto.request.UserProfileUpdateRequest;
import com.titanbank.user.dto.request.UserRegistrationRequest;
import com.titanbank.user.dto.response.UserProfileResponse;
import com.titanbank.user.dto.response.UserRegistrationResponse;
import com.titanbank.user.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    /**
     * Register a new user
     */
    UserRegistrationResponse registerUser(UserRegistrationRequest request);

    /**
     * Get user profile by ID
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * Update user profile
     */
    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);

    /**
     * Upload KYC document
     */
    void uploadKYCDocument(Long userId, MultipartFile file, DocumentType documentType);

    /**
     * Verify user's email
     */
    void verifyEmail(String token);

    /**
     * Initiate password reset
     */
    void initiatePasswordReset(String email);

    /**
     * Reset password with token
     */
    void resetPassword(String token, String newPassword);
}
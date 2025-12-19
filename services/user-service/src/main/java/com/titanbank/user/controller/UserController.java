package com.titanbank.user.controller;

import com.titanbank.user.dto.request.UserProfileUpdateRequest;
import com.titanbank.user.dto.request.UserRegistrationRequest;
import com.titanbank.user.dto.response.UserProfileResponse;
import com.titanbank.user.dto.response.UserRegistrationResponse;
import com.titanbank.user.model.enums.DocumentType;
import com.titanbank.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User profile and KYC operations")
public class UserController {

    private final UserService userService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with email and password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        log.info("Received registration request for email: {}", request.getEmail());
        UserRegistrationResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Get authenticated user's profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get user profile",
            description = "Retrieve authenticated user's profile information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            Authentication authentication) {

        Long userId = Long.parseLong(authentication.getName());
        log.info("Fetching profile for user: {}", userId);

        UserProfileResponse response = userService.getUserProfile(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update user profile",
            description = "Update authenticated user's profile information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        Long userId = Long.parseLong(authentication.getName());
        log.info("Updating profile for user: {}", userId);

        UserProfileResponse response = userService.updateUserProfile(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Profile updated successfully")
        );
    }

    /**
     * Upload KYC document
     */
    @PostMapping(value = "/kyc/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Upload KYC document",
            description = "Upload identity verification document (JPEG, PNG, PDF only, max 10MB)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<String>> uploadKYCDocument(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {

        Long userId = Long.parseLong(authentication.getName());
        log.info("Uploading KYC document for user: {}, type: {}", userId, documentType);

        userService.uploadKYCDocument(userId, file, documentType);

        return ResponseEntity.ok(
                ApiResponse.success("KYC document uploaded successfully")
        );
    }

    /**
     * Verify email address
     */
    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = "Verify user email with token from verification email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam("token") String token) {

        log.info("Verifying email with token");
        userService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully")
        );
    }

    /**
     * Request password reset
     */
    @PostMapping("/password/reset-request")
    @Operation(
            summary = "Request password reset",
            description = "Send password reset email to user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<String>> initiatePasswordReset(
            @RequestParam("email") String email) {

        log.info("Password reset requested for email: {}", email);
        userService.initiatePasswordReset(email);

        return ResponseEntity.ok(
                ApiResponse.success("If the email exists, a password reset link has been sent")
        );
    }

    /**
     * Reset password with token
     */
    @PostMapping("/password/reset")
    @Operation(
            summary = "Reset password",
            description = "Reset password using token from reset email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {

        log.info("Resetting password with token");
        userService.resetPassword(token, newPassword);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully")
        );
    }
}
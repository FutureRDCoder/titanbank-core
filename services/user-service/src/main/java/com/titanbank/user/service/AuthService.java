package com.titanbank.user.service;

import com.titanbank.user.dto.request.LoginRequest;
import com.titanbank.user.dto.response.LoginResponse;

public interface AuthService {

    /**
     * Authenticate user and generate tokens
     */
    LoginResponse login(LoginRequest request);

    /**
     * Generate new access token using refresh token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * Logout user and invalidate tokens
     */
    void logout(String accessToken);
}
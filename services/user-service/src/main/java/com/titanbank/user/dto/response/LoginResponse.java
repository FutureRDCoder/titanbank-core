package com.titanbank.user.dto.response;

import com.titanbank.user.model.enums.*;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn; // seconds

    private UserInfoDTO userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private Set<UserRole> roles;
        private KYCStatus kycStatus;
    }
}
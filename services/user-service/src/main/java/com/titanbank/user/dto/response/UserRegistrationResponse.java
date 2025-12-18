package com.titanbank.user.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
    private Long userId;
    private String email;
    private String message;
}
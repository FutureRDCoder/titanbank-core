package com.titanbank.user.event;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoggedInEvent {
    private Long userId;
    private String email;
    private LocalDateTime timestamp;
}
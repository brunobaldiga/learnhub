package org.example.learnhub.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        String email,
        String username,
        String fullName,
        RoleType roleType,
        LocalDateTime createdAt
) {
}

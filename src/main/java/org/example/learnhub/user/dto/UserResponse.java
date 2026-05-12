package org.example.learnhub.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
       String email,
       String username,
       RoleType roleType,
       LocalDateTime createdAt
) {}

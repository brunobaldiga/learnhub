package org.example.learnhub.user.dto;

import org.example.learnhub.subscription.entity.Subscription;

import java.time.LocalDateTime;

public record UserResponse(
       String email,
       String username,
       Subscription subscription,
       RoleType roleType,
       LocalDateTime createdAt
) {}

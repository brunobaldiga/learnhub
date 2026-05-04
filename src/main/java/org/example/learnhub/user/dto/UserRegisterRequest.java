package org.example.learnhub.user.dto;

public record UserRegisterRequest(
        String username,
        String email,
        String password
) {}
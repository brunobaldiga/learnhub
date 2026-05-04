package org.example.learnhub.user.dto;

public record UserLoginRequest(
        String identifier,
        String password
) {}

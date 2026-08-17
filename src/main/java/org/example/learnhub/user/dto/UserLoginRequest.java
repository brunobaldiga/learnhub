package org.example.learnhub.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank
        String identifier,
        
        @NotBlank
        String password
) {
}

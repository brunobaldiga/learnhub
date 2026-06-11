package org.example.learnhub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank(message = "Username cannot be blank.")
        @Size(
                min = 3,
                max = 20,
                message = "Username must be between 3 and 20 characters long."
        )
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "Username can only contain letters, numbers and underscores."
        )
        String username,

        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Password cannot be blank.")
        @Size(
                min = 6,
                max = 20,
                message = "Password must be between 6 and 20 characters long."
        )
        String password
) {}
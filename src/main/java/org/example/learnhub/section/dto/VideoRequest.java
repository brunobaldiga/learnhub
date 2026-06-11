package org.example.learnhub.section.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VideoRequest(
        @NotBlank(message = "Video URL cannot be blank.")
        @Size(
                min = 10,
                max = 255,
                message = "Video URL must be between 10 and 255 characters long."
        )
        String videoUrl,
        Integer index
) {}

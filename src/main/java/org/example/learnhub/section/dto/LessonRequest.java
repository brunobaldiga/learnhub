package org.example.learnhub.section.dto;

import jakarta.validation.constraints.*;

public record LessonRequest(
        @NotBlank(message = "Lesson URL cannot be blank.")
        @Size(
                min = 10,
                max = 255,
                message = "Lesson URL must be between 10 and 255 characters long."
        )
        String contentUrl,

        @NotNull
        @Min(1)
        @Max(86400)
        Integer duration,

        @NotNull
        @Min(1)
        @Max(20)
        Integer position
) {
}

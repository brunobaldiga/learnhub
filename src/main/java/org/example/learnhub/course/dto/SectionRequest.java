package org.example.learnhub.course.dto;

import jakarta.validation.constraints.*;

public record SectionRequest(
        @NotBlank(message = "Title cannot be blank.")
        @Size(
                min = 3,
                max = 30,
                message = "Section title must be between 3 and 30 characters long."
        )
        String title,

        @NotNull
        @Min(1) @Max(20)
        Integer position
) {
}

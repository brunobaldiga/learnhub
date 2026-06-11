package org.example.learnhub.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionRequest(
        @NotBlank(message = "Title cannot be blank.")
        @Size(
                min = 3,
                max = 30,
                message = "Section title must be between 3 and 30 characters long."
        )
        String title,

        @Min(0) @Max(20)
        Integer index
) {
}

package org.example.learnhub.course.dto;

import jakarta.validation.constraints.*;

public record CourseReviewRequest(
        @NotNull
        @Min(1) @Max(5)
        Integer rating,

        @NotBlank(message = "Review message cannot be blank.")
        @Size(min = 5, max = 500)
        String comment
) {
}

package org.example.learnhub.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseReviewRequest(
        @Min(1) @Max(5)
        Integer rating,

        @NotBlank(message = "Review message cannot be blank.")
        @Size(min = 5, max = 500)
        String comment
) {}

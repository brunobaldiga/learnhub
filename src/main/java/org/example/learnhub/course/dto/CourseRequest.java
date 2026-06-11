package org.example.learnhub.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank(message = "Title cannot be blank.")
        @Size(
                min = 3,
                max = 30,
                message = "Course title must be between 3 and 30 characters long."
        )
        String title
) {}

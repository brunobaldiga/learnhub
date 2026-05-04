package org.example.learnhub.course.dto;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
        @NotBlank(message = "Title cannot be blank.")
        String title
) {}

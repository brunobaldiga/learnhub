package org.example.learnhub.course.dto;

import jakarta.validation.constraints.*;
import org.example.learnhub.course.entity.CourseStatus;

import java.math.BigDecimal;

public record UpdateCourseRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(
                min = 3,
                max = 30,
                message = "Course title must be between 3 and 30 characters long."
        )
        String title,

        @NotNull
        CourseStatus status,

        @DecimalMin("0.0")
        @DecimalMax("999999.99")
        BigDecimal price
) {
}

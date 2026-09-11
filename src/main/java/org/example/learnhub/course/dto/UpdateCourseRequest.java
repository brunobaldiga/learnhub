package org.example.learnhub.course.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;

import java.math.BigDecimal;

public record UpdateCourseRequest(
        @Size(
                min = 3,
                max = 30,
                message = "Course title must be between 3 and 30 characters long."
        )
        String title,

        CourseStatus status,

        @DecimalMin("0.0")
        @DecimalMax("999999.99")
        BigDecimal price,

        CurrencyCode currency
) {
}

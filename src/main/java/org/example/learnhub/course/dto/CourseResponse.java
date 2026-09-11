package org.example.learnhub.course.dto;

import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse(
        Integer id,
        Integer creatorId,
        String creatorUsername,
        String title,
        CourseStatus status,
        BigDecimal price,
        CurrencyCode currency,
        Integer salesAmount,
        LocalDateTime createdAt
) {
}

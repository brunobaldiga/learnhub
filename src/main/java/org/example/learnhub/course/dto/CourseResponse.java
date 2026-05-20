package org.example.learnhub.course.dto;

import org.example.learnhub.course.entity.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse (
    Integer id,
    Integer creatorId,
    String creatorUsername,
    String title,
    CourseStatus status,
    BigDecimal price,
    Integer salesAmount,
    LocalDateTime createdAt
) {}

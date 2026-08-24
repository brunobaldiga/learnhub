package org.example.learnhub.gateway.dto;

import org.example.learnhub.course.entity.CourseStatus;

import java.math.BigDecimal;

public record CourseInfo(
        Integer id,
        Integer creatorId,
        String title,
        BigDecimal price,
        CourseStatus status
) {
}

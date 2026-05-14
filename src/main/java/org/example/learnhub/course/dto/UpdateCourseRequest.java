package org.example.learnhub.course.dto;

import org.example.learnhub.course.entity.CourseStatus;

import java.math.BigDecimal;

public record UpdateCourseRequest(
        String title,
        CourseStatus status,
        BigDecimal price
) {}

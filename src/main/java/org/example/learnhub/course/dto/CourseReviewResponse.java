package org.example.learnhub.course.dto;

import java.time.LocalDateTime;

public record CourseReviewResponse(
        String authorUsername,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}

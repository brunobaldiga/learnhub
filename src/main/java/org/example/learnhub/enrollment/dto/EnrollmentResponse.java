package org.example.learnhub.enrollment.dto;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Integer id,
        Integer courseId,
        String courseTitle,
        String creatorUsername,
        Integer completedLessons,
        Integer totalLessons,
        Double progressPercentage,
        LocalDateTime enrolledAt
) {
}

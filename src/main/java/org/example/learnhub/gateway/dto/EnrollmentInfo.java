package org.example.learnhub.gateway.dto;

import java.time.LocalDateTime;

public record EnrollmentInfo(
        Integer id,
        Integer userId,
        Integer courseId,
        LocalDateTime enrolledAt
) {
}

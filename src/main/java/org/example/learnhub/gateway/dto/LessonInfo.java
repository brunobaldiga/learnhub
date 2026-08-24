package org.example.learnhub.gateway.dto;

import java.time.LocalDateTime;

public record LessonInfo(
        Integer id,
        String contentUrl,
        Integer position,
        LocalDateTime createdAt
) {
}

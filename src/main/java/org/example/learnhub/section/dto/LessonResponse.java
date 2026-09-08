package org.example.learnhub.section.dto;

import java.time.LocalDateTime;

public record LessonResponse(
        Integer id,
        String contentUrl,
        Integer duration,
        Integer position,
        LocalDateTime createdAt
) {
}

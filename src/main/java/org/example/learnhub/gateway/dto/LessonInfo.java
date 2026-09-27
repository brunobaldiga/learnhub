package org.example.learnhub.gateway.dto;

public record LessonInfo(
        Integer id,
        Integer duration,
        Integer sectionId,
        Integer courseId
) {
}

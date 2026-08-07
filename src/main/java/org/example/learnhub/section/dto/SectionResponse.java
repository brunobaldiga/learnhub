package org.example.learnhub.section.dto;

import java.util.List;

public record SectionResponse(
        Integer id,
        String title,
        Integer position,
        List<LessonResponse> lessons
) {
}

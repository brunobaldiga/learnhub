package org.example.learnhub.section.service;

import org.example.learnhub.section.dto.LessonRequest;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.entity.Lesson;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {
    public Lesson toLesson(LessonRequest request) {
        return Lesson.builder()
                .contentUrl(request.contentUrl())
                .position(request.position())
                .build();
    }


    public LessonResponse toDto(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getContentUrl(),
                lesson.getPosition(),
                lesson.getCreatedAt()
        );
    }
}

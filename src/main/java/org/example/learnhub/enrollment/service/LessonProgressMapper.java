package org.example.learnhub.enrollment.service;

import org.example.learnhub.enrollment.dto.ProgressRequest;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.entity.LessonProgress;
import org.example.learnhub.section.entity.Lesson;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LessonProgressMapper {
    public LessonProgress toLessonProgress(ProgressRequest request, Enrollment enrollment, Lesson lesson) {
        LocalDateTime now = LocalDateTime.now();

        return LessonProgress.builder()
                .lastPositionInSeconds(request.lastPositionInSeconds())
                .enrollment(enrollment)
                .lesson(lesson)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}

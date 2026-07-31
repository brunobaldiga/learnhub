package org.example.learnhub.lesson.service;

import org.example.learnhub.lesson.dto.ProgressRequest;
import org.example.learnhub.lesson.dto.ProgressResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LessonService {
    public ProgressResponse progress(Integer lessonId, ProgressRequest request) {
        Optional<LessonProgress> lessonProgress = repository.findByLessonId(lessonId);
        Lesson

        if (lessonProgress.isEmpty()) {
            lessonProgress = mapper.toLessonProgress(request);
        }


    }
}

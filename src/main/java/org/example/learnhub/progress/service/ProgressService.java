package org.example.learnhub.progress.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.progress.dto.ProgressRequest;
import org.example.learnhub.progress.dto.ProgressResponse;
import org.example.learnhub.progress.entity.LessonProgress;
import org.example.learnhub.section.entity.Lesson;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {
    private final LessonGateway lessonGateway;

    public ProgressResponse progress(Integer lessonId, ProgressRequest request) {
        Lesson lesson = lessonGateway.findLessonById(lessonId);

        Optional<LessonProgress> lessonProgress = repository.findByLessonId(lessonId);

        if (lessonProgress.isEmpty()) {
            lessonProgress = mapper.toLessonProgress(request);
        }



        return new ProgressResponse(

        );

    }
}

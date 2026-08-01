package org.example.learnhub.gateway;

import org.example.learnhub.section.entity.Lesson;

public interface LessonGateway {
    Lesson findLessonById(Integer lessonId);
}

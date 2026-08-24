package org.example.learnhub.gateway;

import org.example.learnhub.gateway.dto.LessonInfo;

public interface LessonGateway {
    LessonInfo findLessonById(Integer lessonId);
}

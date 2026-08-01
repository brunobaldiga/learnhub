package org.example.learnhub.section.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.service.SectionService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonGatewayImpl implements LessonGateway {
    private final SectionService service;

    @Override
    public Lesson findLessonById(Integer lessonId) {
        return service.findLessonEntityById(lessonId);
    }
}

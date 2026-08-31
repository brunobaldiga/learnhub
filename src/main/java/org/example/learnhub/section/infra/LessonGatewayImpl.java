package org.example.learnhub.section.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.gateway.dto.LessonInfo;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.service.SectionService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonGatewayImpl implements LessonGateway {
    private final SectionService service;

    @Override
    public Integer calculateDurationByCourseId(Integer courseId) {
        return service.calculateDurationByCourseId(courseId);
    }

    @Override
    public LessonInfo findById(Integer lessonId) {
        Lesson lesson = service.findLessonEntityById(lessonId);

        return new LessonInfo(
                lessonId,
                lesson.getDuration(),
                lesson.getSection().getId(),
                lesson.getSection().getCourseId()
        );
    }
}

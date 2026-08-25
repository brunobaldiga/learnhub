package org.example.learnhub.course.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseGatewayImpl implements CourseGateway {
    private final CourseService service;

    @Override
    public boolean isCourseCreator(Integer courseId, Integer creatorId) {
        return service.isCourseCreator(courseId, creatorId);
    }

    @Override
    public CourseInfo findCourseById(Integer userId, Integer courseId) {
        Course course = service.findCourseEntityById(userId, courseId);

        return new CourseInfo(
                course.getId(),
                course.getCreatorId(),
                course.getTitle(),
                course.getPrice(),
                course.getStatus()
        );
    }

    @Override
    public Integer countLessonsByCourseId(Integer courseId) {
        return service.countLessonsByCourseId(courseId);
    }

    @Override
    public void incrementSalesAmount(Integer courseId) {
        service.incrementSalesAmount(courseId);
    }

    @Override
    public void recordReview(Integer courseId, Integer rating) {
        service.recordReview(courseId, rating);
    }
}

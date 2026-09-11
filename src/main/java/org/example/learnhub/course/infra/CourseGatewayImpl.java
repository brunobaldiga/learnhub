package org.example.learnhub.course.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.CourseSummary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CourseGatewayImpl implements CourseGateway {
    private final CourseService service;

    @Override
    public boolean isCourseCreator(Integer courseId, Integer creatorId) {
        return service.isCourseCreator(courseId, creatorId);
    }

    @Override
    public Map<Integer, CourseSummary> findCourseSummariesById(Collection<Integer> courseIds) {
        return service.findCourseSummariesById(courseIds);
    }

    @Override
    public CourseInfo findById(Integer userId, Integer courseId) {
        Course course = service.findCourseEntityById(userId);

        return new CourseInfo(
                course.getId(),
                course.getCreatorId(),
                course.getTitle(),
                course.getPrice(),
                course.getCurrency(),
                course.getStatus()
        );
    }

    @Override
    public Integer countLessonsByCourseId(Integer courseId) {
        return service.countLessonsByCourseId(courseId);
    }

    @Override
    public CourseSummary findCourseSummaryById(Integer courseId) {
        return service.findCourseSummaryById(courseId);
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

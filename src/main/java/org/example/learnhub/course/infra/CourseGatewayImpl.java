package org.example.learnhub.course.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseGatewayImpl implements CourseGateway {
    private final CourseService service;

    @Override
    public Course findCourseById(User user, Integer courseId) {
        return service.findCourseEntityById(user, courseId);
    }

    @Override
    public Integer countVideosByCourseId(Integer courseId) {
        return service.countVideosByCourseId(courseId);
    }
}

package org.example.learnhub.enrollment.gateway;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.user.entity.User;

public interface CourseGateway {
    Course findCourseById(User user, Integer courseId);

    Integer countVideosByCourseId(Integer courseId);
}

package org.example.learnhub.gateway;

import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.user.entity.User;

public interface CourseGateway {
    CourseInfo findCourseById(User user, Integer courseId);

    Integer countLessonsByCourseId(Integer courseId);

    void incrementSalesAmount(Integer courseId);

    void recordReview(Integer courseId, Integer rating);
}

package org.example.learnhub.gateway;

import org.example.learnhub.gateway.dto.CourseInfo;

public interface CourseGateway {
    CourseInfo findCourseById(Integer userId, Integer courseId);

    Integer countLessonsByCourseId(Integer courseId);

    void incrementSalesAmount(Integer courseId);

    void recordReview(Integer courseId, Integer rating);

    boolean isCourseCreator(Integer courseId, Integer creatorId);
}

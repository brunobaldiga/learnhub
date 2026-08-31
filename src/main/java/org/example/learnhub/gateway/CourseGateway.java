package org.example.learnhub.gateway;

import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.CourseSummary;

import java.util.Collection;
import java.util.Map;

public interface CourseGateway {
    CourseInfo findById(Integer userId, Integer courseId);

    Integer countLessonsByCourseId(Integer courseId);

    CourseSummary findCourseSummaryById(Integer courseId);

    void incrementSalesAmount(Integer courseId);

    void recordReview(Integer courseId, Integer rating);

    boolean isCourseCreator(Integer courseId, Integer creatorId);

    Map<Integer, CourseSummary> findCourseSummariesById(Collection<Integer> courseIds);
}

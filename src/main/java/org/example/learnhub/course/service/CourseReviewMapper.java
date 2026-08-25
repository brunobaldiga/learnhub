package org.example.learnhub.course.service;

import org.example.learnhub.course.dto.CourseReviewRequest;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseReview;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CourseReviewMapper {
    public CourseReview toCourseReview(User user, Course course, CourseReviewRequest request) {
        return CourseReview.builder()
                .course(course)
                .authorId(user.getId())
                .rating(request.rating())
                .comment(request.comment())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public CourseReviewResponse toDto(CourseReview courseReview, String authorUsername) {
        return new CourseReviewResponse(
                authorUsername,
                courseReview.getRating(),
                courseReview.getComment(),
                courseReview.getCreatedAt()
        );
    }
}

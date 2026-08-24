package org.example.learnhub.course.service;

import org.example.learnhub.course.dto.CourseReviewRequest;
import org.example.learnhub.course.dto.CourseReviewResponse;
import org.example.learnhub.course.entity.CourseReview;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CourseReviewMapper {

    public CourseReview toCourseReview(User user, CourseInfo course, CourseReviewRequest request) {
        return CourseReview.builder()
                .course(course)
                .author(user)
                .rating(request.rating())
                .comment(request.comment())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public CourseReviewResponse toDto(CourseReview courseReview) {
        return new CourseReviewResponse(
                courseReview.getAuthor().getUsername(),
                courseReview.getRating(),
                courseReview.getComment(),
                courseReview.getCreatedAt()
        );
    }
}

package org.example.learnhub.course.service;

import org.example.learnhub.course.dto.CourseRequest;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.dto.UpdateCourseRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CourseMapper {
    public Course toCourse(CourseRequest request) {
        return Course.builder()
                .title(request.title())
                .status(CourseStatus.PRIVATE)
                .build();
    }

    public CourseResponse toDto(Course course, String creatorUsername) {
        return new CourseResponse(
                course.getId(),
                course.getCreatorId(),
                creatorUsername,
                course.getTitle(),
                course.getStatus(),
                course.getPrice(),
                course.getCurrency(),
                course.getSalesAmount(),
                course.getCreatedAt()
        );
    }

    public void updateCourse(Course course, UpdateCourseRequest request) {
        Optional.ofNullable(request.title()).ifPresent(course::setTitle);
        Optional.ofNullable(request.status()).ifPresent(course::setStatus);
        Optional.ofNullable(request.price()).ifPresent(course::setPrice);
        Optional.ofNullable(request.currency()).ifPresent(course::setCurrency);
    }
}

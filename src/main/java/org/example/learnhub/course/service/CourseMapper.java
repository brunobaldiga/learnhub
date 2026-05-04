package org.example.learnhub.course.service;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.dto.CourseRequest;
import org.example.learnhub.course.dto.CourseResponse;
import org.example.learnhub.course.entity.CourseStatus;
import org.springframework.stereotype.Service;


@Service
public class CourseMapper {
    public Course toCourse(CourseRequest request) {
        return Course.builder()
                .title(request.title())
                .status(CourseStatus.PRIVATE)
                .build();
    }

    public CourseResponse toDto(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCreator().getId(),
                course.getCreator().getUsername(),
                course.getTitle(),
                course.getStatus(),
                course.getSections(),
                course.getCreatedAt()
        );
    }
}

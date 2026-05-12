package org.example.learnhub.enrollment.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.service.CourseService;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.gateway.CourseGateway;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository repository;
    private final EnrollmentMapper mapper;
    private final CourseGateway courseGateway;

    public void enroll(User user, Integer courseId) {
        Course course = courseGateway.findCourseById(user, courseId);

        Optional<Enrollment> existingCourseProgress = repository.findByUserAndCourse(user, course);

        if (existingCourseProgress.isPresent()) throw new RuntimeException("User is already enrolled.");

        Enrollment courseProgress = Enrollment.builder()
                .user(user)
                .course(course)
                .totalLessons(courseGateway.countVideosByCourseId(courseId))
                .build();

        repository.save(courseProgress);
    }

    public Page<EnrollmentResponse> getEnrolledCourses(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return repository.findByEnrollmentsUserId(userId, pageable)
                .map(mapper::toDto);
    }
}

package org.example.learnhub.enrollment;

import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.enrollment.service.EnrollmentMapper;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {
    @Mock
    private EnrollmentRepository repository;

    @Mock
    private CourseGateway courseGateway;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private EnrollmentService service;

    @Test
    void shouldEnrollCourseSuccessfully() {

    };

    @Test
    void shouldThrowWhenCourseNotFound() {}

    @Test
    void shouldThrowWhenUserHasNotPaidToEnrollCourse() {}

    @Test
    void shouldReturnEnrolledCoursesSuccessfully() {}

    @Test
    void shouldReturnEnrollmentSuccessfully() {}

    @Test
    void shouldThrowWhenEnrollmentDoesNotExists() {}

}
package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.Course;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.UserAlreadyEnrolled;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.enrollment.service.EnrollmentMapper;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.user.dto.RoleType;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {
    @Mock
    private EnrollmentRepository repository;

    @Mock
    private CourseGateway courseGateway;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private EnrollmentService service;

    private User user;

    private Course course;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("John").roleType(RoleType.USER).build();
        course = Course.builder().id(1).build();

    }

    @Test
    void shouldEnrollCourseSuccessfully() {
        Optional<Enrollment> optionalEnrollment = Optional.empty();

        when(courseGateway.findCourseById(any(), any())).thenReturn(course);
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(true);
        when(repository.findByUserAndCourse(user, course)).thenReturn(optionalEnrollment);

        service.enroll(user, 1);
        verify(repository).save(any(Enrollment.class));
    };

    @Test
    void shouldReturn403WhenUserHasNotPaidToEnrollCourse() {
        when(courseGateway.findCourseById(any(), any())).thenReturn(course);
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.enroll(user, 1))
                .isInstanceOf(CourseAccessDenied.class)
                .hasMessage("User haven't bought the course.");
    }

    @Test
    void shouldReturn409WhenUserHasAlreadyEnrolledCourse() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();

        when(courseGateway.findCourseById(any(), any())).thenReturn(course);
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(true);

        when(repository.findByUserAndCourse(any(), any())).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.enroll(user, 1))
                .isInstanceOf(UserAlreadyEnrolled.class)
                .hasMessage("User is already enrolled.");
    }

    @Test
    void shouldReturnEnrolledCoursesSuccessfully() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        Page<Enrollment> enrollments = new PageImpl<>(List.of(enrollment));

        EnrollmentResponse response = new EnrollmentResponse(1, 1, "Java Course", "Creator",  10, 0, 0.0, LocalDateTime.now());

        when(repository.findByUserId(any(), any())).thenReturn(enrollments);
        when(mapper.toDto(any())).thenReturn(response);

        Page<EnrollmentResponse> result = service.findEnrolledCourses(1, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);

        verify(repository).findByUserId(eq(1), any(Pageable.class));
        verify(mapper).toDto(enrollment);
    }

    @Test
    void shouldReturnEnrollmentSuccessfully() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        EnrollmentResponse response = new EnrollmentResponse(1, 1, "Java Course", "Creator",  10, 0, 0.0, LocalDateTime.now());

        when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(enrollment));
        when(mapper.toDto(any())).thenReturn(response);

        EnrollmentResponse result = service.findEnrollmentById(user.getId(), enrollment.getId());

        assertThat(result).isEqualTo(response);

        verify(repository).findByIdAndUserId(user.getId(), enrollment.getId());
        verify(mapper).toDto(enrollment);
    }

    @Test
    void shouldReturn404WhenEnrollmentDoesNotExists() {
        when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEnrollmentById(user.getId(), 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Enrollment not found.");
    }

}
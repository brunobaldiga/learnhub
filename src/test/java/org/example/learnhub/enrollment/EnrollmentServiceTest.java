package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.repository.CertificateRepository;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.enrollment.repository.LessonProgressRepository;
import org.example.learnhub.enrollment.service.CertificateMapper;
import org.example.learnhub.enrollment.service.EnrollmentMapper;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.UserAlreadyEnrolled;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.CourseSummary;
import org.example.learnhub.gateway.dto.LessonInfo;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {
    @Mock
    private EnrollmentRepository repository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private LessonGateway lessonGateway;

    @Mock
    private CertificateMapper certificateMapper;

    @Mock
    private CourseGateway courseGateway;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private EnrollmentMapper mapper;

    @InjectMocks
    private EnrollmentService service;

    private User user;
    private CourseInfo courseInfo;
    private CourseSummary courseSummary;
    private LessonInfo lessonInfo;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("John").roleType(RoleType.USER).build();
        courseInfo = new CourseInfo(1, 2, "Java Course", BigDecimal.TEN, CourseStatus.PUBLIC);
        courseSummary = new CourseSummary(1, "Java Course", "Creator");
        lessonInfo = new LessonInfo(1, 100, 1, 1);
        enrollment = Enrollment.builder().id(1).userId(user.getId()).courseId(1).build();
    }

    @Test
    void shouldEnrollCourseSuccessfully() {
        Optional<Enrollment> optionalEnrollment = Optional.empty();

        when(courseGateway.findById(any(), any())).thenReturn(courseInfo);
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(true);
        when(repository.findByUserIdAndCourseId(user.getId(), courseInfo.id())).thenReturn(optionalEnrollment);

        service.enroll(user, 1);
        verify(repository).save(any(Enrollment.class));
    }

    @Test
    void shouldReturn403WhenUserHasNotPaidToEnrollCourse() {
        when(courseGateway.findById(any(), any())).thenReturn(courseInfo);
        when(paymentGateway.existsByUserIdAndCourseId(user.getId(), courseInfo.id())).thenReturn(false);

        assertThatThrownBy(() -> service.enroll(user, 1))
                .isInstanceOf(CourseAccessDenied.class)
                .hasMessage("User haven't bought the course.");
    }

    @Test
    void shouldReturn409WhenUserHasAlreadyEnrolledCourse() {
        when(courseGateway.findById(user.getId(), courseInfo.id())).thenReturn(courseInfo);
        when(paymentGateway.existsByUserIdAndCourseId(user.getId(), courseInfo.id())).thenReturn(true);
        when(repository.findByUserIdAndCourseId(user.getId(), courseInfo.id())).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.enroll(user, 1))
                .isInstanceOf(UserAlreadyEnrolled.class)
                .hasMessage("User already enrolled.");
    }

    @Test
    void shouldReturnEnrolledCoursesSuccessfully() {
        Page<Enrollment> enrollments = new PageImpl<>(List.of(enrollment));
        EnrollmentResponse response = new EnrollmentResponse(1, 1, "Java Course", "Creator", 10, 10, 100.0, LocalDateTime.now());

        when(repository.findByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(enrollments);
        when(courseGateway.findCourseSummariesById(Set.of(enrollment.getCourseId())))
                .thenReturn(Map.of(enrollment.getCourseId(), courseSummary));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId())).thenReturn(10);
        when(lessonGateway.)
    }


}
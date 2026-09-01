package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.enrollment.dto.CertificateResponse;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.dto.ProgressRequest;
import org.example.learnhub.enrollment.dto.ProgressResponse;
import org.example.learnhub.enrollment.entity.Certificate;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.entity.LessonProgress;
import org.example.learnhub.enrollment.repository.CertificateRepository;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.enrollment.repository.LessonProgressRepository;
import org.example.learnhub.enrollment.service.CertificateMapper;
import org.example.learnhub.enrollment.service.EnrollmentMapper;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.exception.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(mapper.toDto(enrollment, courseSummary, 2, 4)).thenReturn(response);

        Page<EnrollmentResponse> result = service.findEnrolledCourses(1, 0, 10);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldReturn404WhenEnrollmentDoesNotExist() {
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEnrollmentById(1, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Enrollment not found");
    }

    @Test
    void shouldStartLessonSuccessfully() {
        LessonInfo lesson = new LessonInfo(20, 100, 3, 10);
        when(lessonGateway.findById(20)).thenReturn(lesson);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(1);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);

        ProgressResponse result = service.startLesson(user, 20);

        verify(lessonProgressRepository).save(any(LessonProgress.class));
        assertThat(result.completedLessons()).isEqualTo(1);
        assertThat(result.totalLessons()).isEqualTo(4);
        assertThat(result.courseCompleted()).isFalse();
    }


    @Test
    void shouldStartLessonWithoutCreatingDuplicateProgress() {
        LessonInfo lesson = new LessonInfo(20, 100, 3, 10);
        when(lessonGateway.findById(20)).thenReturn(lesson);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(4);
        when(lessonProgressRepository.existsByLessonIdAndEnrollmentId(20, 1)).thenReturn(true);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);

        ProgressResponse result = service.startLesson(user, 20);

        verify(lessonProgressRepository, never()).save(any());
        assertThat(result.courseCompleted()).isTrue();
    }

    @Test
    void shouldReturn404WhenStartingLessonWithoutEnrollment() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startLesson(user, 20))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Enrollment not found.");
    }

    @Test
    void shouldUpdateLessonProgressSuccessfully() {
        LessonInfo lesson = new LessonInfo(20, 100, 3, 10);
        LessonProgress progress = LessonProgress.builder()
                .id(5).enrollment(enrollment).lessonId(20).lastPositionInSeconds(10)
                .completed(false).createdAt(LocalDateTime.now().minusMinutes(1)).updatedAt(LocalDateTime.now().minusSeconds(1)).build();
        when(lessonGateway.findById(20)).thenReturn(lesson);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 1)).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(0);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(2);

        ProgressResponse result = service.progress(user, 20, new ProgressRequest(15));

        verify(lessonProgressRepository).save(progress);
        assertThat(progress.getLastPositionInSeconds()).isEqualTo(15);
        assertThat(progress.getCompleted()).isFalse();
        assertThat(result.completedLessons()).isZero();
    }

    @Test
    void shouldMarkLessonCompletedWhenProgressReachesNinetyPercent() {
        LessonInfo lesson = new LessonInfo(20, 100, 3, 10);
        LessonProgress progress = LessonProgress.builder()
                .id(5).enrollment(enrollment).lessonId(20).lastPositionInSeconds(80)
                .completed(false).createdAt(LocalDateTime.now().minusMinutes(1)).updatedAt(LocalDateTime.now()).build();
        when(lessonGateway.findById(20)).thenReturn(lesson);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 1)).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(0);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(1);

        ProgressResponse result = service.progress(user, 20, new ProgressRequest(90));

        assertThat(progress.getCompleted()).isTrue();
        assertThat(result.completedLessons()).isEqualTo(1);
        assertThat(result.courseCompleted()).isTrue();
    }

    @Test
    void shouldReturn400WhenProgressBeyondLessonDuration() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));

        assertThatThrownBy(() -> service.progress(user, 20, new ProgressRequest(101)))
                .isInstanceOf(InvalidLessonProgressException.class)
                .hasMessage("Progress cannot exceed the lesson duration.");
    }

    @Test
    void shouldReturn404WhenProgressWithoutLessonProgress() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.progress(user, 20, new ProgressRequest(20)))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Lesson progress not found.");
    }

    @Test
    void shouldGenerateCertificateWhenCourseIsCompleted() {
        Certificate certificate = Certificate.builder().id(UUID.randomUUID()).enrollment(enrollment)
                .fullNameAtIssuance("John Doe").courseTitleAtIssuance("Java Course")
                .courseLengthInHoursAtIssuance(2).issuedAt(LocalDate.now()).build();
        CertificateResponse response = new CertificateResponse(certificate.getId(), "John Doe", "Java Course", 2, certificate.getIssuedAt());
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(4);
        when(certificateRepository.existsByEnrollmentId(1)).thenReturn(false);
        when(lessonGateway.calculateDurationByCourseId(10)).thenReturn(7200);
        when(courseGateway.findCourseSummaryById(10)).thenReturn(courseSummary);
        when(certificateMapper.toCertificate(user, enrollment, "Java Course", 7200)).thenReturn(certificate);
        when(certificateMapper.toDto(certificate)).thenReturn(response);

        assertThat(service.generateCertificate(user, 1)).isEqualTo(response);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void shouldReturnCertificateWhenCourseIsIncomplete() {
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(3);

        assertThatThrownBy(() -> service.generateCertificate(user, 1))
                .isInstanceOf(CourseNotCompletedException.class);
    }

    @Test
    void shouldReturn403WhenDuplicateCertificate() {
        when(repository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(1)).thenReturn(4);
        when(certificateRepository.existsByEnrollmentId(1)).thenReturn(true);

        assertThatThrownBy(() -> service.generateCertificate(user, 1))
                .isInstanceOf(DuplicateCertificateException.class);
    }

    @Test
    void shouldReturnCertificateByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        Certificate certificate = Certificate.builder().id(id).build();
        CertificateResponse response = new CertificateResponse(id, "John Doe", "Java Course", 2, LocalDate.now());
        when(certificateRepository.findById(id)).thenReturn(Optional.of(certificate));
        when(certificateMapper.toDto(certificate)).thenReturn(response);

        assertThat(service.findCertificateById(id)).isEqualTo(response);
    }

    @Test
    void shouldReturn404WhenCertificateDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(certificateRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCertificateById(id))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Certificate not found.");
    }

    @Test
    void shouldCheckEnrollmentExistence() {
        when(repository.existsByUserIdAndCourseId(1, 10)).thenReturn(true);

        assertThat(service.existsByUserIdAndCourseId(1, 10)).isTrue();
    }
}
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
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {
    @Mock
    EnrollmentRepository repository;
    @Mock
    EnrollmentMapper mapper;
    @Mock
    CourseGateway courseGateway;
    @Mock
    PaymentGateway paymentGateway;
    @Mock
    LessonGateway lessonGateway;
    @Mock
    LessonProgressRepository lessonProgressRepository;
    @Mock
    CertificateRepository certificateRepository;
    @Mock
    CertificateMapper certificateMapper;

    @InjectMocks
    EnrollmentService service;

    private User user;
    private CourseInfo courseInfo;
    private CourseSummary courseSummary;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("student").fullName("John Doe").build();
        courseInfo = new CourseInfo(10, 2, "Java Course", BigDecimal.TEN, CurrencyCode.USD, CourseStatus.PUBLIC);
        courseSummary = new CourseSummary(10, "Java Course", "creator");
        enrollment = Enrollment.builder().id(5).userId(1).courseId(10).enrolledAt(LocalDateTime.now()).build();
    }

    @Test
    void shouldEnrollPaidUser() {
        when(courseGateway.findById(10)).thenReturn(courseInfo);
        when(paymentGateway.existsByUserIdAndCourseId(1, 10)).thenReturn(true);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.empty());

        service.enroll(user, 10);

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1);
        assertThat(captor.getValue().getCourseId()).isEqualTo(10);
    }

    @Test
    void shouldRejectEnrollmentWithoutPayment() {
        when(courseGateway.findById(10)).thenReturn(courseInfo);
        when(paymentGateway.existsByUserIdAndCourseId(1, 10)).thenReturn(false);

        assertThatThrownBy(() -> service.enroll(user, 10))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("User haven't bought the course.");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateEnrollment() {
        when(courseGateway.findById(10)).thenReturn(courseInfo);
        when(paymentGateway.existsByUserIdAndCourseId(1, 10)).thenReturn(true);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.enroll(user, 10))
                .isInstanceOf(UserAlreadyEnrolledException.class)
                .hasMessage("User is already enrolled.");
    }

    @Test
    void shouldFindEnrolledCourses() {
        PageRequest pageable = PageRequest.of(0, 10);
        EnrollmentResponse response = new EnrollmentResponse(
                5, 10, "Java Course", "creator", 2, 4, 50.0, enrollment.getEnrolledAt()
        );
        when(repository.findByUserId(1, pageable)).thenReturn(new PageImpl<>(List.of(enrollment), pageable, 1));
        when(courseGateway.findCourseSummariesById(Set.of(10))).thenReturn(Map.of(10, courseSummary));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(2);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(mapper.toDto(enrollment, courseSummary, 2, 4)).thenReturn(response);

        Page<EnrollmentResponse> result = service.findEnrolledCourses(1, pageable);

        assertThat(result.getContent()).containsExactly(response);
    }

    @Test
    void shouldFindEnrollmentById() {
        EnrollmentResponse response = new EnrollmentResponse(
                5, 10, "Java Course", "creator", 1, 4, 25.0, enrollment.getEnrolledAt()
        );
        when(repository.findByIdAndUserId(5, 1)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(1);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(courseGateway.findCourseSummaryById(10)).thenReturn(courseSummary);
        when(mapper.toDto(enrollment, courseSummary, 1, 4)).thenReturn(response);

        assertThat(service.findEnrollmentById(1, 5)).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenEnrollmentDoesNotExist() {
        when(repository.findByIdAndUserId(5, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEnrollmentById(1, 5))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Enrollment not found.");
    }

    @Test
    void shouldStartLessonAndCreateProgress() {
        LessonInfo lesson = new LessonInfo(20, 100, 3, 10);
        when(lessonGateway.findById(20)).thenReturn(lesson);
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(1);
        when(lessonProgressRepository.existsByLessonIdAndEnrollmentId(20, 5)).thenReturn(false);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);

        ProgressResponse result = service.startLesson(user, 20);

        ArgumentCaptor<LessonProgress> captor = ArgumentCaptor.forClass(LessonProgress.class);
        verify(lessonProgressRepository).save(captor.capture());
        assertThat(captor.getValue().getLessonId()).isEqualTo(20);
        assertThat(captor.getValue().getEnrollment()).isSameAs(enrollment);
        assertThat(result.completedLessons()).isEqualTo(1);
        assertThat(result.totalLessons()).isEqualTo(4);
        assertThat(result.completedPercentage()).isEqualTo(25.0);
        assertThat(result.courseCompleted()).isFalse();
    }

    @Test
    void shouldNotCreateDuplicateLessonProgress() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(4);
        when(lessonProgressRepository.existsByLessonIdAndEnrollmentId(20, 5)).thenReturn(true);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);

        ProgressResponse result = service.startLesson(user, 20);

        verify(lessonProgressRepository, never()).save(any());
        assertThat(result.courseCompleted()).isTrue();
    }

    @Test
    void shouldRejectStartingLessonWithoutEnrollment() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startLesson(user, 20))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Enrollment not found.");
    }

    @Test
    void shouldUpdateLessonProgress() {
        LessonProgress progress = LessonProgress.builder()
                .id(30)
                .enrollment(enrollment)
                .lessonId(20)
                .lastPositionInSeconds(10)
                .completed(false)
                .updatedAt(LocalDateTime.now().minusSeconds(5))
                .build();
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 5)).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(0);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(2);

        ProgressResponse result = service.progress(user, 20, new ProgressRequest(15));

        assertThat(progress.getLastPositionInSeconds()).isEqualTo(15);
        assertThat(progress.getCompleted()).isFalse();
        assertThat(result.completedLessons()).isZero();
        verify(lessonProgressRepository).save(progress);
    }

    @Test
    void shouldMarkLessonCompletedAtNinetyPercent() {
        LessonProgress progress = LessonProgress.builder()
                .id(30)
                .enrollment(enrollment)
                .lessonId(20)
                .lastPositionInSeconds(80)
                .completed(false)
                .updatedAt(LocalDateTime.now().minusSeconds(1))
                .build();
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 5)).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(0);
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(1);

        ProgressResponse result = service.progress(user, 20, new ProgressRequest(90));

        assertThat(progress.getCompleted()).isTrue();
        assertThat(result.completedLessons()).isEqualTo(1);
        assertThat(result.courseCompleted()).isTrue();
    }

    @Test
    void shouldRejectProgressBeyondLessonDuration() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));

        assertThatThrownBy(() -> service.progress(user, 20, new ProgressRequest(101)))
                .isInstanceOf(InvalidLessonProgressException.class)
                .hasMessage("Progress cannot exceed the lesson duration.");
    }

    @Test
    void shouldRejectUnrealisticallyFastProgress() {
        LessonProgress progress = LessonProgress.builder()
                .id(30)
                .enrollment(enrollment)
                .lessonId(20)
                .lastPositionInSeconds(0)
                .completed(false)
                .updatedAt(LocalDateTime.now())
                .build();
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 5)).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(0);

        assertThatThrownBy(() -> service.progress(user, 20, new ProgressRequest(50)))
                .isInstanceOf(InvalidLessonProgressException.class)
                .hasMessage("Progress exceeds the maximum allowed position.");
    }

    @Test
    void shouldThrowWhenLessonProgressDoesNotExist() {
        when(lessonGateway.findById(20)).thenReturn(new LessonInfo(20, 100, 3, 10));
        when(repository.findByUserIdAndCourseId(1, 10)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(20, 5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.progress(user, 20, new ProgressRequest(20)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Lesson progress not found.");
    }

    @Test
    void shouldGenerateCertificateForCompletedCourse() {
        UUID id = UUID.randomUUID();
        Certificate certificate = Certificate.builder()
                .id(id)
                .enrollment(enrollment)
                .fullNameAtIssuance("John Doe")
                .courseTitleAtIssuance("Java Course")
                .courseLengthInHoursAtIssuance(2)
                .issuedAt(LocalDate.now())
                .build();
        CertificateResponse response = new CertificateResponse(id, "John Doe", "Java Course", 2, LocalDate.now());

        when(repository.findByIdAndUserId(5, 1)).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(4);
        when(certificateRepository.existsByEnrollmentId(5)).thenReturn(false);
        when(lessonGateway.calculateDurationByCourseId(10)).thenReturn(7200);
        when(courseGateway.findCourseSummaryById(10)).thenReturn(courseSummary);
        when(certificateMapper.toCertificate(user, enrollment, "Java Course", 7200)).thenReturn(certificate);
        when(certificateMapper.toDto(certificate)).thenReturn(response);

        assertThat(service.generateCertificate(user, 5)).isEqualTo(response);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void shouldRejectCertificateBeforeCourseCompletion() {
        when(repository.findByIdAndUserId(5, 1)).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(3);

        assertThatThrownBy(() -> service.generateCertificate(user, 5))
                .isInstanceOf(CourseNotCompletedException.class)
                .hasMessage("Cannot generate certificate, user did not finish the course.");
    }

    @Test
    void shouldRejectDuplicateCertificate() {
        when(repository.findByIdAndUserId(5, 1)).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(10)).thenReturn(4);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(5)).thenReturn(4);
        when(certificateRepository.existsByEnrollmentId(5)).thenReturn(true);

        assertThatThrownBy(() -> service.generateCertificate(user, 5))
                .isInstanceOf(DuplicateCertificateException.class)
                .hasMessage("User cannot generate more than 1 certificate per course.");
    }

    @Test
    void shouldFindCertificateById() {
        UUID id = UUID.randomUUID();
        Certificate certificate = Certificate.builder().id(id).build();
        CertificateResponse response = new CertificateResponse(id, "John Doe", "Java Course", 2, LocalDate.now());
        when(certificateRepository.findById(id)).thenReturn(Optional.of(certificate));
        when(certificateMapper.toDto(certificate)).thenReturn(response);

        assertThat(service.findCertificateById(id)).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenCertificateDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(certificateRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCertificateById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Certificate not found.");
    }
}

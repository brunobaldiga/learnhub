package org.example.learnhub.enrollment;

import org.example.learnhub.course.entity.Course;
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
import org.example.learnhub.enrollment.service.LessonProgressMapper;
import org.example.learnhub.exception.*;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
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
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    private LessonProgressMapper lessonProgressMapper;

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
    private Course course;
    private Lesson lesson;
    private Section section;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("John").roleType(RoleType.USER).build();
        course = Course.builder().id(1).build();
        section = Section.builder().id(1).course(course).build();
        lesson = Lesson.builder().id(1).section(section).duration(100).build();
    }

    @Test
    void shouldEnrollCourseSuccessfully() {
        Optional<Enrollment> optionalEnrollment = Optional.empty();

        when(courseGateway.findCourseById(any(), any())).thenReturn(course);
        when(paymentGateway.existsByUserIdAndCourseId(any(), any())).thenReturn(true);
        when(repository.findByUserAndCourse(user, course)).thenReturn(optionalEnrollment);

        service.enroll(user, 1);
        verify(repository).save(any(Enrollment.class));
    }

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

        EnrollmentResponse response = new EnrollmentResponse(1, 1, "Java Course", "Creator", 10, 10, 0.0, LocalDateTime.now());

        when(repository.findByUserId(any(), any())).thenReturn(enrollments);
        when(mapper.toDto(any(), any(), any())).thenReturn(response);

        Page<EnrollmentResponse> result = service.findEnrolledCourses(1, 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);

        verify(repository).findByUserId(eq(1), any(Pageable.class));
    }

    @Test
    void shouldReturnEnrollmentSuccessfully() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        EnrollmentResponse response = new EnrollmentResponse(1, 1, "Java Course", "Creator", 10, 10, 0.0, LocalDateTime.now());

        when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(enrollment));
        when(mapper.toDto(any(), any(), any())).thenReturn(response);

        EnrollmentResponse result = service.findEnrollmentById(user.getId(), enrollment.getId());

        assertThat(result).isEqualTo(response);

        verify(repository).findByIdAndUserId(user.getId(), enrollment.getId());
    }

    @Test
    void shouldReturn404WhenEnrollmentDoesNotExists() {
        when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEnrollmentById(user.getId(), 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Enrollment not found.");
    }

    @Test
    void shouldCreateLessonProgressSuccessfully() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        ProgressRequest request = mock(ProgressRequest.class);
        LessonProgress lessonProgress = mock(LessonProgress.class);

        when(lessonGateway.findLessonById(1)).thenReturn(lesson);
        when(repository.findByCourseIdAndUserId(1, user.getId())).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(1, enrollment.getId())).thenReturn(Optional.empty());
        when(lessonProgressMapper.toLessonProgress(request, enrollment, lesson)).thenReturn(lessonProgress);

        ProgressResponse result = service.progress(user, 1, request);

        assertThat(result).isNotNull();

        verify(lessonProgressMapper).toLessonProgress(request, enrollment, lesson);
        verify(lessonProgressRepository).save(lessonProgress);
    }

    @Test
    void shouldReturn404WhenProgressEnrollmentDoesNotExist() {
        ProgressRequest request = mock(ProgressRequest.class);

        when(lessonGateway.findLessonById(1)).thenReturn(lesson);
        when(repository.findByCourseIdAndUserId(1, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.progress(user, 1, request))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Enrollment not found.");

        verify(lessonProgressRepository, never()).findByLessonIdAndEnrollmentId(any(), any());
        verify(lessonProgressRepository, never()).save(any());
    }

    @Test
    void shouldUpdateLessonProgressSuccessfully() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        LessonProgress lessonProgress = mock(LessonProgress.class);
        ProgressRequest request = mock(ProgressRequest.class);

        when(lessonGateway.findLessonById(1)).thenReturn(lesson);
        when(repository.findByCourseIdAndUserId(1, user.getId())).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(1, enrollment.getId())).thenReturn(Optional.of(lessonProgress));
        when(lessonProgress.getUpdatedAt()).thenReturn(LocalDateTime.now().minusSeconds(5));
        when(lessonProgress.getLastPositionInSeconds()).thenReturn(10);
        when(request.lastPositionInSeconds()).thenReturn(20);

        ProgressResponse result = service.progress(user, 1, request);

        assertThat(result).isNotNull();

        verify(lessonProgress).setLastPositionInSeconds(20);
        verify(lessonProgress).setUpdatedAt(any(LocalDateTime.class));
        verify(lessonProgressRepository).save(lessonProgress);
    }

    @Test
    void shouldReturnInvalidProgressWhenPositionExceedsMaximumAllowed() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        LessonProgress lessonProgress = mock(LessonProgress.class);
        ProgressRequest request = mock(ProgressRequest.class);

        when(lessonGateway.findLessonById(1)).thenReturn(lesson);
        when(repository.findByCourseIdAndUserId(1, user.getId())).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.findByLessonIdAndEnrollmentId(1, enrollment.getId())).thenReturn(Optional.of(lessonProgress));
        when(lessonProgress.getUpdatedAt()).thenReturn(LocalDateTime.now().minusSeconds(5));
        when(lessonProgress.getLastPositionInSeconds()).thenReturn(10);
        when(request.lastPositionInSeconds()).thenReturn(50);

        assertThatThrownBy(() -> service.progress(user, 1, request))
                .isInstanceOf(InvalidLessonProgressException.class)
                .hasMessage("Progress exceeds the maximum allowed position.");

        verify(lessonProgressRepository, never()).save(lessonProgress);
    }

    @Test
    void shouldReturnInvalidProgressWhenPositionExceedsLessonDuration() {
        LessonProgress lessonProgress = mock(LessonProgress.class);
        ProgressRequest request = mock(ProgressRequest.class);

        when(lessonGateway.findLessonById(1)).thenReturn(lesson);
        when(request.lastPositionInSeconds()).thenReturn(150);

        assertThatThrownBy(() -> service.progress(user, 1, request))
                .isInstanceOf(InvalidLessonProgressException.class)
                .hasMessage("Progress cannot exceed the lesson duration.");

        verify(lessonProgressRepository, never()).save(lessonProgress);
    }

    @Test
    void shouldGenerateCertificateSuccessfully() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();
        Certificate certificate = mock(Certificate.class);
        CertificateResponse response = mock(CertificateResponse.class);

        when(repository.findByIdAndUserId(1, user.getId())).thenReturn(Optional.of(enrollment));
        when(certificateRepository.existsByEnrollmentId(1)).thenReturn(false);
        when(certificateMapper.toCertificate(user, enrollment)).thenReturn(certificate);
        when(certificateMapper.toDto(certificate)).thenReturn(response);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(any())).thenReturn(10);

        CertificateResponse result = service.generateCertificate(user, 1);

        assertThat(result).isEqualTo(response);

        verify(certificateRepository).existsByEnrollmentId(1);
        verify(certificateMapper).toCertificate(user, enrollment);
        verify(certificateRepository).save(certificate);
        verify(certificateMapper).toDto(certificate);
    }

    @Test
    void shouldReturn404WhenGeneratingCertificateForMissingEnrollment() {
        when(repository.findByIdAndUserId(1, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateCertificate(user, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Enrollment not found.");

        verify(certificateRepository, never()).existsByEnrollmentId(any());
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void shouldReturnExceptionWhenCourseIsNotCompleted() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();

        when(repository.findByIdAndUserId(1, user.getId())).thenReturn(Optional.of(enrollment));
        when(courseGateway.countLessonsByCourseId(enrollment.getCourse().getId())).thenReturn(10);

        assertThatThrownBy(() -> service.generateCertificate(user, 1))
                .isInstanceOf(CourseNotCompletedException.class)
                .hasMessage("Cannot generate certificate, user did not finish the course.");

        verify(certificateRepository, never()).existsByEnrollmentId(any());
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void shouldReturnExceptionWhenCertificateAlreadyExists() {
        Enrollment enrollment = Enrollment.builder().id(1).user(user).course(course).build();

        when(repository.findByIdAndUserId(1, user.getId())).thenReturn(Optional.of(enrollment));
        when(certificateRepository.existsByEnrollmentId(1)).thenReturn(true);
        when(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(any())).thenReturn(10);

        assertThatThrownBy(() -> service.generateCertificate(user, 1))
                .isInstanceOf(DuplicateCertificateException.class)
                .hasMessage("User cannot generate more than 1 certificate per course.");

        verify(certificateRepository).existsByEnrollmentId(1);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void shouldReturnCertificateSuccessfully() {
        UUID certificateId = UUID.randomUUID();
        Certificate certificate = mock(Certificate.class);
        CertificateResponse response = mock(CertificateResponse.class);

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.of(certificate));
        when(certificateMapper.toDto(certificate)).thenReturn(response);

        CertificateResponse result = service.findCertificateById(certificateId);

        assertThat(result).isEqualTo(response);

        verify(certificateRepository).findById(certificateId);
        verify(certificateMapper).toDto(certificate);
    }

    @Test
    void shouldReturn404WhenCertificateDoesNotExist() {
        UUID certificateId = UUID.randomUUID();

        when(certificateRepository.findById(certificateId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCertificateById(certificateId))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Certificate not found.");

        verify(certificateRepository).findById(certificateId);
        verify(certificateMapper, never()).toDto(any());
    }
}
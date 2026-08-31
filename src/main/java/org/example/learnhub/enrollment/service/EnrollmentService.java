package org.example.learnhub.enrollment.service;

import lombok.RequiredArgsConstructor;
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
import org.example.learnhub.exception.*;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.CourseSummary;
import org.example.learnhub.gateway.dto.LessonInfo;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository repository;
    private final EnrollmentMapper mapper;
    private final CourseGateway courseGateway;
    private final PaymentGateway paymentGateway;
    private final LessonGateway lessonGateway;
    private final LessonProgressRepository lessonProgressRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateMapper certificateMapper;

    @Transactional
    public void enroll(User user, Integer courseId) {
        CourseInfo course = courseGateway.findById(user.getId(), courseId);

        if(!paymentGateway.existsByUserIdAndCourseId(user.getId(), courseId))
            throw new CourseAccessDenied("User haven't bought the course.");

        Optional<Enrollment> existingCourseProgress = repository.findByUserIdAndCourseId(user.getId(), course.id());

        if(existingCourseProgress.isPresent()) throw new UserAlreadyEnrolled("User is already enrolled.");

        Enrollment courseProgress = Enrollment.builder()
                .userId(user.getId())
                .courseId(course.id())
                .build();

        repository.save(courseProgress);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> findEnrolledCourses(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> enrollments = repository.findByUserId(userId, pageable);

        Set<Integer> courseIds = enrollments.getContent().stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());

        Map<Integer, CourseSummary> summariesByCourseId = courseGateway.findCourseSummariesById(courseIds);

        return enrollments.map(enrollment -> mapper.toDto(
                enrollment,
                summariesByCourseId.get(enrollment.getCourseId()),
                lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId()),
                courseGateway.countLessonsByCourseId(enrollment.getCourseId())
        ));
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse findEnrollmentById(Integer userId, Integer enrollmentId) {
        Enrollment enrollment = repository.findByIdAndUserId(enrollmentId, userId)
                .orElseThrow(() -> new EntityNotFound("Enrollment not found."));

        Integer completedLessons = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollmentId);
        Integer totalLessons = courseGateway.countLessonsByCourseId(enrollment.getCourseId());
        CourseSummary courseSummary = courseGateway.findCourseSummaryById(enrollment.getCourseId());

        return mapper.toDto(enrollment, courseSummary, completedLessons, totalLessons);
    }

    @Transactional
    public ProgressResponse startLesson(User user, Integer lessonId) {
        LessonInfo lessonInfo = lessonGateway.findById(lessonId);

        Enrollment enrollment = repository.findByUserIdAndCourseId(user.getId(), lessonInfo.courseId())
                .orElseThrow(() -> new EntityNotFound("Enrollment not found."));

        Integer completedLessons = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        if(!lessonProgressRepository.existsByLessonIdAndEnrollmentId(lessonId, enrollment.getId())) {
            LocalDateTime now = LocalDateTime.now();

            LessonProgress newLessonProgress = LessonProgress.builder()
                    .lastPositionInSeconds(0)
                    .enrollment(enrollment)
                    .lessonId(lessonInfo.id())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            lessonProgressRepository.save(newLessonProgress);
        }

        Integer totalLessons = courseGateway.countLessonsByCourseId(lessonInfo.courseId());

        return new ProgressResponse(
                completedLessons,
                totalLessons,
                (completedLessons * 100.0) / totalLessons,
                totalLessons.equals(completedLessons)
        );
    }

    @Transactional
    public ProgressResponse progress(User user, Integer lessonId, ProgressRequest request) {
        LessonInfo lessonInfo = lessonGateway.findById(lessonId);

        if(request.lastPositionInSeconds() > lessonInfo.duration())
            throw new InvalidLessonProgressException("Progress cannot exceed the lesson duration.");

        Enrollment enrollment = repository.findByUserIdAndCourseId(user.getId(), lessonInfo.courseId())
                .orElseThrow(() -> new EntityNotFound("Enrollment not found."));

        LessonProgress lessonProgress = lessonProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollment.getId())
                .orElseThrow(() -> new EntityNotFound("Lesson progress not found."));

        Integer completedLessons = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        long elapsedSeconds = Duration.between(lessonProgress.getUpdatedAt(), LocalDateTime.now()).toSeconds();
        int maxAllowed = lessonProgress.getLastPositionInSeconds() + (int) elapsedSeconds + 10;

        if(request.lastPositionInSeconds() > maxAllowed)
            throw new InvalidLessonProgressException("Progress exceeds the maximum allowed position.");
        if(request.lastPositionInSeconds() > lessonInfo.duration())
            throw new InvalidLessonProgressException("Progress cannot exceed the lesson duration.");

        lessonProgress.setUpdatedAt(LocalDateTime.now());
        lessonProgress.setLastPositionInSeconds(request.lastPositionInSeconds());

        if(!lessonProgress.getCompleted() && lessonProgress.getLastPositionInSeconds() >= lessonInfo.duration() * .9) {
            lessonProgress.setCompleted(true);
            completedLessons++;

        }

        lessonProgressRepository.save(lessonProgress);

        Integer totalLessons = courseGateway.countLessonsByCourseId(lessonInfo.courseId());

        return new ProgressResponse(
                completedLessons,
                totalLessons,
                (completedLessons * 100.0) / totalLessons,
                totalLessons.equals(completedLessons)
        );
    }

    @Transactional
    public CertificateResponse generateCertificate(User user, Integer enrollmentId) {
        Enrollment enrollment = repository.findByIdAndUserId(enrollmentId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Enrollment not found."));

        Integer totalLessons = courseGateway.countLessonsByCourseId(enrollment.getCourseId());

        if(lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollmentId) < totalLessons)
            throw new CourseNotCompletedException("Cannot generate certificate, user did not finish the course.");

        if(certificateRepository.existsByEnrollmentId(enrollment.getId()))
            throw new DuplicateCertificateException("User cannot generate more than 1 certificate per course.");

        Integer courseDuration = lessonGateway.calculateDurationByCourseId(enrollment.getCourseId());
        CourseSummary courseSummary = courseGateway.findCourseSummaryById(enrollment.getCourseId());

        Certificate certificate = certificateMapper.toCertificate(user, enrollment, courseSummary.title(), courseDuration);

        certificateRepository.save(certificate);

        return certificateMapper.toDto(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateResponse findCertificateById(UUID certificateId) {
        return certificateMapper.toDto(
                certificateRepository.findById(certificateId)
                        .orElseThrow(() -> new EntityNotFound("Certificate not found."))
        );
    }

    @Transactional(readOnly = true)
    public Optional<Enrollment> findEnrollmentEntityByUserIdAndCourseId(Integer userId, Integer courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId);
    }

    public boolean existsByUserIdAndCourseId(Integer userId, Integer courseId) {
        return repository.existsByUserIdAndCourseId(userId, courseId);
    }
}

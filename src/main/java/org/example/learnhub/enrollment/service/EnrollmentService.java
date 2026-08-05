package org.example.learnhub.enrollment.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.enrollment.entity.LessonProgress;
import org.example.learnhub.enrollment.repository.LessonProgressRepository;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.InvalidLessonProgressException;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.enrollment.repository.EnrollmentRepository;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.UserAlreadyEnrolled;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.LessonGateway;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.enrollment.dto.ProgressRequest;
import org.example.learnhub.enrollment.dto.ProgressResponse;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository repository;
    private final EnrollmentMapper mapper;
    private final CourseGateway courseGateway;
    private final PaymentGateway paymentGateway;
    private final LessonGateway lessonGateway;
    private final LessonProgressMapper lessonProgressMapper;
    private final LessonProgressRepository lessonProgressRepository;

    public void enroll(User user, Integer courseId) {
        Course course = courseGateway.findCourseById(user, courseId);

        if (!paymentGateway.existsByUserIdAndCourseId(user.getId(), courseId)) throw new CourseAccessDenied("User haven't bought the course.");

        Optional<Enrollment> existingCourseProgress = repository.findByUserAndCourse(user, course);

        if (existingCourseProgress.isPresent()) throw new UserAlreadyEnrolled("User is already enrolled.");

        Enrollment courseProgress = Enrollment.builder()
                .user(user)
                .course(course)
                .totalLessons(courseGateway.countLessonsByCourseId(courseId))
                .build();

        repository.save(courseProgress);
    }

    public Page<EnrollmentResponse> findEnrolledCourses(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return repository.findByUserId(userId, pageable)
                .map(mapper::toDto);
    }

    public EnrollmentResponse findEnrollmentById(Integer userId, Integer enrollmentId) {
        return mapper.toDto(repository.findByIdAndUserId(enrollmentId, userId)
                .orElseThrow(() -> new EntityNotFound("Enrollment not found.")));
    }

    public ProgressResponse progress(User user, Integer lessonId, ProgressRequest request) {
        Lesson lesson = lessonGateway.findLessonById(lessonId);

        Enrollment enrollment = repository.findByCourseIdAndUserId(lesson.getSection().getCourse().getId(), user.getId())
                .orElseThrow(() -> new EntityNotFound("Enrollment not found."));

        Optional<LessonProgress> existing = lessonProgressRepository.findByLessonIdAndEnrollmentId(lessonId, enrollment.getId());

        if (existing.isEmpty()) {
            LessonProgress newLessonProgress = lessonProgressMapper.toLessonProgress(request, enrollment, lesson);

            lessonProgressRepository.save(newLessonProgress);

            return new ProgressResponse(
                    enrollment.getCompletedLessons(),
                    enrollment.getTotalLessons(),
                    (enrollment.getCompletedLessons() * 100.0) / enrollment.getTotalLessons(),
                    enrollment.getTotalLessons().equals(enrollment.getCompletedLessons())
            );
        }

        LessonProgress lessonProgress = existing.get();

        long elapsedSeconds = Duration.between(lessonProgress.getUpdatedAt(), LocalDateTime.now()).toSeconds();
        int maxAllowed = lessonProgress.getLastPositionInSeconds() + (int) elapsedSeconds + 10;

        if (request.lastPositionInSeconds() > maxAllowed) throw new InvalidLessonProgressException("Progress exceeds the maximum allowed position.");
        if (request.lastPositionInSeconds() > lesson.getDuration()) throw new InvalidLessonProgressException("Progress cannot exceed the lesson duration.");

        lessonProgress.setUpdatedAt(LocalDateTime.now());
        lessonProgress.setLastPositionInSeconds(request.lastPositionInSeconds());

        lessonProgressRepository.save(lessonProgress);

        return new ProgressResponse(
                enrollment.getCompletedLessons(),
                enrollment.getTotalLessons(),
                (enrollment.getCompletedLessons() * 100.0) / enrollment.getTotalLessons(),
                enrollment.getTotalLessons().equals(enrollment.getCompletedLessons())
        );
    }
}

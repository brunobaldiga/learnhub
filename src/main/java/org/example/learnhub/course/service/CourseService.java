package org.example.learnhub.course.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseProgress;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseProgressRepository;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.repository.CourseSpecs;
import org.example.learnhub.user.entity.User;
import org.example.learnhub.course.dto.CourseFilter;
import org.example.learnhub.course.dto.CourseRequest;
import org.example.learnhub.course.dto.CourseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final CourseProgressRepository courseProgressRepository;

    public CourseResponse create(User user, CourseRequest request) {
        Course course = mapper.toCourse(request);
        course.setCreator(user);

        repository.save(course);

        return mapper.toDto(course);
    }

    public Page<CourseResponse> search(CourseFilter filter, Pageable pageable) {
        Specification<Course> specification = Specification
                .where(CourseSpecs.withFilter(filter))
                .and(CourseSpecs.isPublic());

        Page<Course> courses = repository.findAll(specification, pageable);

        return courses.map(mapper::toDto);
    }

    public Page<CourseResponse> getEnrolledCourses(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return repository.findByEnrollmentsUserId(userId, pageable)
                .map(mapper::toDto);
    }

    public void enroll(User user, Integer courseId) {
        Course course = repository.findByIdAndStatus(courseId, CourseStatus.PUBLIC)
                .orElseThrow(() -> new RuntimeException("Course not found."));

        Optional<CourseProgress> existingCourseProgress = courseProgressRepository.findByUserAndCourse(user, course);

        if (existingCourseProgress.isPresent()) throw new RuntimeException("User is already enrolled.");

        CourseProgress courseProgress = CourseProgress.builder()
                .user(user)
                .course(course)
                .totalLessons(courseProgressRepository.countVideosByCourseId(courseId))
                .build();

        courseProgressRepository.save(courseProgress);
    }

    public void unenroll(User user, Integer courseId) {
        CourseProgress courseProgress = courseProgressRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found."));;

        courseProgressRepository.delete(courseProgress);
    }

    public Page<CourseResponse> getCourses(User user, CourseFilter filter, Pageable pageable) {
        Specification<Course> specification = Specification
                .where(CourseSpecs.withFilter(filter))
                .and(CourseSpecs.ownedBy(user.getId()));

        Page<Course> courses = repository.findAll(specification, pageable);

        return courses.map(mapper::toDto);
    }

    public CourseResponse findById(User user, Integer courseId) {
        Course course = repository.findByIdAndStatus(courseId, CourseStatus.PUBLIC)
                .or(() -> repository.findByIdAndCreatorId(courseId, user.getId()))
                .orElseThrow(() -> new RuntimeException("Course not found."));

        return mapper.toDto(course);
    }
}

package org.example.learnhub.course.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;

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

    public Page<CourseResponse> findCourses(User user, CourseFilter filter, Pageable pageable) {
        Specification<Course> specification = Specification
                .where(CourseSpecs.withFilter(filter))
                .and(CourseSpecs.ownedBy(user.getId()));

        Page<Course> courses = repository.findAll(specification, pageable);

        return courses.map(mapper::toDto);
    }

    public CourseResponse findCourseById(User user, Integer courseId) {
        return mapper.toDto(findCourseEntityById(user, courseId));
    }

    public Course findCourseEntityById(User user, Integer courseId) {
        return repository.findByIdAndStatus(courseId, CourseStatus.PUBLIC)
                .or(() -> repository.findByIdAndCreatorId(courseId, user.getId()))
                .orElseThrow(() -> new RuntimeException("Course not found."));
    }

    public Integer countVideosByCourseId(Integer courseId) {
        return repository.countVideosByCourseId(courseId);
    }
}

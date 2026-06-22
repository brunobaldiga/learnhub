package org.example.learnhub.course.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.repository.CourseSpecs;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.MaxSectionsReached;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final SectionGateway sectionGateway;
    private final PaymentGateway paymentGateway;
    private final SectionMapper sectionMapper;

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
                .orElseThrow(() -> new EntityNotFound("Course not found."));
    }

    public Integer countVideosByCourseId(Integer courseId) {
        return repository.countVideosByCourseId(courseId);
    }

    public CourseResponse updateCourseById(User user, Integer courseId, UpdateCourseRequest request) {
        Course course = repository.findByIdAndCreatorId(courseId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Course not found."));

        mapper.updateCourse(course, request);
        repository.save(course);

        return mapper.toDto(course);
    }

    public SectionResponse createCourseSection(User user, Integer courseId, SectionRequest request) {
        Course course = repository.findByIdAndCreatorId(courseId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Course not found."));

        if (course.getSections().size() >= 20) throw new MaxSectionsReached("Course cannot have more than 20 sections.");

        Section section = sectionGateway.createSection(request, course);
        course.getSections().add(section);

        repository.save(course);

        return sectionGateway.toDto(section);
    }

    public List<SectionResponse> findCourseSection(User user, Integer courseId) {
        Course course = findCourseEntityById(user, courseId);

        boolean hasPaid = paymentGateway.existsByUserIdAndCourseId(user.getId(), courseId);
        boolean isOwner = course.getCreator().getId().equals(user.getId());

        if (!hasPaid && !isOwner) throw new CourseAccessDenied("User haven't paid for the course");

        return course.getSections().stream()
                .map(sectionGateway::toDto)
                .toList();
    }

    public SectionResponse updateCourseSection(User user, Integer sectionId, SectionRequest request) {
        Section section = sectionGateway.findByIdAndCourseCreatorId(sectionId, user.getId());

        section.setTitle(request.title());
        section.setIndex(request.index());

        sectionGateway.saveSection(section);

        return sectionMapper.toDto(section);
    }

    public void deleteCourseSection(User user, Integer sectionId) {
        Section section = sectionGateway.findByIdAndCourseCreatorId(sectionId, user.getId());
        sectionGateway.deleteSection(section);
    }
}

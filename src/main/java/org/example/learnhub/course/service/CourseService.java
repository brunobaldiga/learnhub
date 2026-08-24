package org.example.learnhub.course.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.*;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.repository.CourseRepository;
import org.example.learnhub.course.repository.CourseSpecs;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.exception.MaxSectionsReached;
import org.example.learnhub.gateway.PaymentGateway;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final SectionGateway sectionGateway;
    private final PaymentGateway paymentGateway;

    @Transactional
    public CourseResponse create(User user, CourseRequest request) {
        Course course = mapper.toCourse(request);
        course.setCreatorId(user.getId());

        repository.save(course);

        return mapper.toDto(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> search(CourseFilter filter, Pageable pageable) {
        Specification<Course> specification = Specification
                .where(CourseSpecs.withFilter(filter))
                .and(CourseSpecs.isPublic());

        Page<Course> courses = repository.findAll(specification, pageable);

        return courses.map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> findCourses(User user, CourseFilter filter, Pageable pageable) {
        Specification<Course> specification = Specification
                .where(CourseSpecs.withFilter(filter))
                .and(CourseSpecs.ownedBy(user.getId()));

        Page<Course> courses = repository.findAll(specification, pageable);

        return courses.map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public CourseResponse findCourseById(User user, Integer courseId) {
        return mapper.toDto(findCourseEntityById(user, courseId));
    }

    public Course findCourseEntityById(User user, Integer courseId) {
        return repository.findByIdAndStatus(courseId, CourseStatus.PUBLIC)
                .or(() -> repository.findByIdAndCreatorId(courseId, user.getId()))
                .orElseThrow(() -> new EntityNotFound("Course not found."));
    }

    @Transactional(readOnly = true)
    public Integer countLessonsByCourseId(Integer courseId) {
        return repository.countLessonsByCourseId(courseId);
    }

    @Transactional
    public CourseResponse updateCourseById(User user, Integer courseId, UpdateCourseRequest request) {
        Course course = repository.findByIdAndCreatorId(courseId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Course not found."));

        mapper.updateCourse(course, request);

        return mapper.toDto(course);
    }

    @Transactional
    public SectionResponse createCourseSection(User user, Integer courseId, SectionRequest request) {
        Course course = repository.findByIdAndCreatorId(courseId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Course not found."));

        if(sectionGateway.countSectionsByCourseId(courseId) >= 20)
            throw new MaxSectionsReached("Course cannot have more than 20 sections.");

        SectionInfo section = sectionGateway.createSection(request, courseId);

        repository.save(course);

        return new SectionResponse(
                section.id(),
                section.title(),
                section.position(),
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findCourseSection(User user, Integer courseId) {
        Course course = findCourseEntityById(user, courseId);

        boolean hasPaid = paymentGateway.existsByUserIdAndCourseId(user.getId(), courseId);
        boolean isOwner = course.getCreatorId().equals(user.getId());

        if(!hasPaid && !isOwner) throw new CourseAccessDenied("User haven't paid for the course");

        return sectionGateway.findAllByCourseId(courseId);
    }

    public SectionResponse updateCourseSection(User user, Integer sectionId, SectionRequest request) {
        SectionInfo sectionInfo = sectionGateway.updateSection(sectionId, user.getId(), request);

        return new SectionResponse(
                sectionId,
                sectionInfo.title(),
                sectionInfo.position(),
                sectionGateway.findLessonsBySectionId(sectionId) weiofwmeiofjwioejfiowjeiofjweiofowie
        )
    }

    @Transactional
    public void deleteCourseSection(User user, Integer sectionId) {
        Section section = sectionGateway.findByIdAndCourseCreatorId(sectionId, user.getId());
        sectionGateway.deleteSection(section);
    }

    @Transactional
    public void incrementSalesAmount(Integer courseId) {
        Course course = repository.findById(courseId)
                .orElseThrow(() -> new EntityNotFound("Course not found."));
        course.setSalesAmount(course.getSalesAmount() + 1);
        repository.save(course);
    }


    public void recordReview(Integer courseId, Integer rating) {
        Course course = repository.findById(courseId)
                .orElseThrow(() -> new EntityNotFound("Course not found."));

        course.addReview(rating);
        repository.save(course);
    }
}

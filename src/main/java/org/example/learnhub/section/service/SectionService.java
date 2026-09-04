package org.example.learnhub.section.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.LessonRequest;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.repository.LessonRepository;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository repository;
    private final LessonRepository lessonRepository;
    private final SectionMapper mapper;
    private final LessonMapper lessonMapper;
    private final EnrollmentGateway enrollmentGateway;
    private final CourseGateway courseGateway;

    @Transactional
    public SectionInfo create(SectionRequest request, Integer courseId) {
        Section section = mapper.toSection(request, courseId);

        return mapper.toSectionInfo(repository.save(section));
    }

    @Transactional
    public SectionResponse createLesson(User user, Integer sectionId, LessonRequest request) {
        Section section = findSectionEntityByIdAndCourseCreatorId(sectionId, user.getId());
        Lesson lesson = lessonMapper.toLesson(request);

        section.addLesson(lesson);

        repository.save(section);

        return mapper.toDto(section);
    }

    @Transactional
    public SectionResponse update(Integer sectionId, Integer creatorId, SectionRequest request) {
        Section section = repository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFound("Section not found."));

        if(!courseGateway.isCourseCreator(section.getCourseId(), creatorId))
            throw new CourseAccessDenied("You are not the creator of this course.");

        section.setTitle(request.title());
        section.setPosition(request.position());
        Section saved = repository.save(section);

        return mapper.toDto(saved);
    }

    @Transactional
    public void deleteLesson(User user, Integer sectionId, Integer lessonId) {
        Section section = findSectionEntityByIdAndCourseCreatorId(sectionId, user.getId());

        Lesson lesson = section.getLessons()
                .stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst().orElseThrow(() -> new EntityNotFound("Lesson not found."));

        section.getLessons().remove(lesson);

        repository.save(section);
    }

    @Transactional
    public void delete(Integer sectionId, Integer creatorId) {
        Section section = findSectionEntityByIdAndCourseCreatorId(sectionId, creatorId);
        repository.delete(section);
    }

    @Transactional(readOnly = true)
    public LessonResponse findLessonById(User user, Integer lessonId) {
        Lesson lesson = findLessonEntityById(lessonId);

        CourseInfo courseInfo = courseGateway.findById(user.getId(), lesson.getSection().getCourseId());

        boolean isCourseCreator = courseInfo.creatorId().equals(user.getId());
        boolean isEnrolled = enrollmentGateway.existsByUserIdAndCourseId(user.getId(), courseInfo.id());

        if(!isCourseCreator && !isEnrolled) throw new CourseAccessDenied("User does not have access to this course.");

        return lessonMapper.toDto(lesson);
    }

    @Transactional(readOnly = true)
    public Lesson findLessonEntityById(Integer lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(
                () -> new EntityNotFound("Lesson not found."));
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> findSectionLessons(User user, Integer sectionId) {
        Section section = repository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFound("Section not found"));

        CourseInfo courseInfo = courseGateway.findById(user.getId(), section.getCourseId());

        boolean isCourseCreator = courseInfo.creatorId().equals(user.getId());
        boolean isEnrolled = enrollmentGateway.findByUserIdAndCourseId(user.getId(), courseInfo.id()).isPresent();

        if(!isCourseCreator && !isEnrolled)
            throw new CourseAccessDenied("User does not have access to this course.");

        return lessonRepository.findAllBySectionId(section.getId())
                .stream().map(lessonMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Section findSectionEntityByIdAndCourseCreatorId(Integer sectionId, Integer creatorId) {
        Section section = findSectionEntityByIdAndCourseCreatorId(sectionId, user.getId());

        CourseInfo courseInfo = courseGateway.findBySectionId(sectionId);
        return repository.findById(sectionId, creatorId)
                .orElseThrow(() -> new EntityNotFound("Section not found"));
    }

    public Integer calculateDurationByCourseId(Integer courseId) {
        return repository.calculateDurationByCourseId(courseId);
    }
}

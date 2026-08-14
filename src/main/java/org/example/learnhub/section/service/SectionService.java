package org.example.learnhub.section.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.enrollment.entity.Enrollment;
import org.example.learnhub.exception.CourseAccessDenied;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.section.dto.LessonRequest;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.repository.LessonRepository;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository repository;
    private final LessonRepository lessonRepository;
    private final SectionMapper mapper;
    private final LessonMapper lessonMapper;
    private final EnrollmentGateway enrollmentGateway;

    public Section saveSection(Section section) {
        return repository.save(section);
    }

    public Section createSection(SectionRequest request, Course course) {
        Section section = mapper.toSection(request, course);

        return repository.save(section);
    }

    public SectionResponse createLesson(User user, Integer sectionId, LessonRequest request) {
        Section section = findSectionEntityByIdAndCourseCreatorId(sectionId, user.getId());
        Lesson lesson = lessonMapper.toLesson(request);

        section.addLesson(lesson);

        repository.save(section);

        return mapper.toDto(section);
    }

    public void deleteLesson(User user, Integer sectionId, Integer lessonId) {
        Section section = findSectionEntityByIdAndCourseCreatorId(sectionId, user.getId());

        section.getLessons().removeIf(lesson -> lesson.getId().equals(lessonId));

        repository.save(section);
    }

    public Section findSectionEntityByIdAndCourseCreatorId(Integer sectionId, Integer creatorId) {
        return repository.findByIdAndCourseCreatorId(sectionId, creatorId)
                .orElseThrow(() -> new EntityNotFound("Section not found"));
    }

    public void deleteSection(Section section) {
        repository.delete(section);
    }

    public LessonResponse findLessonById(User user, Integer lessonId) {
        Lesson lesson = findLessonEntityById(lessonId);

        Course course = lesson.getSection().getCourse();

        boolean isCourseCreator = course.getCreator().getId().equals(user.getId());
        boolean isEnrolled = enrollmentGateway.findEnrollmentByUserIdAndCourseId(user.getId(), course.getId()).isPresent();

        if(!isCourseCreator && !isEnrolled) throw new CourseAccessDenied("User does not have access to this course.");

        return lessonMapper.toDto(lesson);
    }

    public Lesson findLessonEntityById(Integer lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(
                () -> new EntityNotFound("Lesson not found."));
    }

    public List<LessonResponse> findSectionLessons(User user, Integer sectionId) {
        Section section = repository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFound("Section not found"));

        boolean isCourseCreator = section.getCourse().getCreator().getId().equals(user.getId());
        Optional<Enrollment> enrollment = enrollmentGateway.findEnrollmentByUserIdAndCourseId(user.getId(), section.getCourse().getId());

        if(enrollment.isEmpty() && !isCourseCreator)
            throw new CourseAccessDenied("User does not have access to this course.");

        return lessonRepository.findAllBySectionId(section.getId())
                .stream().map(lessonMapper::toDto).toList();
    }
}

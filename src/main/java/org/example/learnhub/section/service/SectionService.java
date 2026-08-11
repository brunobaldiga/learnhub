package org.example.learnhub.section.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.enrollment.entity.Enrollment;
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

    public LessonResponse findLessonById(Integer lessonId) {
        return lessonMapper.toDto(findLessonEntityById(lessonId));
    }

    public Lesson findLessonEntityById(Integer lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(
                () -> new EntityNotFound("Lesson not found."));
    }

    public List<LessonResponse> findSectionLessons(User user, Integer sectionId) {
        Section section = repository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFound("Section not found"));

        Enrollment enrollment = enrollmentGateway.findEnrollmentByUserIdAndCourseId(user.getId(), )
    }
}

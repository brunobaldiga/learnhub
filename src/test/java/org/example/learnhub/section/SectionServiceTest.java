package org.example.learnhub.section;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.exception.CourseAccessDeniedException;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.exception.MaxLessonsReachedException;
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
import org.example.learnhub.section.service.LessonMapper;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceTest {
    @Mock
    private SectionRepository repository;

    @Mock
    private SectionMapper mapper;

    @Mock
    private LessonMapper lessonMapper;

    @Mock
    private EnrollmentGateway enrollmentGateway;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseGateway courseGateway;

    @InjectMocks
    private SectionService service;

    private User creator;
    private User student;
    private Section section;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1).username("creator").build();
        student = User.builder().id(2).username("student").build();
        section = Section.builder().id(10).title("Introduction").position(1).courseId(20).lessons(new ArrayList<>()).build();
    }

    @Test
    void shouldCreateSectionSuccessfully() {
        SectionRequest request = new SectionRequest("Introduction", 1);
        when(mapper.toSection(request, 20)).thenReturn(section);
        when(repository.save(section)).thenReturn(section);
        SectionInfo info = new SectionInfo(10, "Introduction", 1, 20);
        when(mapper.toSectionInfo(section)).thenReturn(info);

        assertThat(service.createSection(request, 20)).isEqualTo(info);
        verify(repository).save(section);
    }

    @Test
    void shouldCreateLessonForCourseCreatorSuccessfully() {
        LessonRequest request = new LessonRequest("https://example.com/lesson", 120, 1);
        Lesson lesson = Lesson.builder().id(30).contentUrl(request.contentUrl()).duration(120).position(1).build();
        SectionResponse response = new SectionResponse(10, "Introduction", 1, List.of(new LessonResponse(30, request.contentUrl(), 1, null)));
        when(repository.findByIdAndCourseCreatorId(10, 1)).thenReturn(Optional.of(section));
        when(lessonMapper.toLesson(request)).thenReturn(lesson);
        when(mapper.toDto(section)).thenReturn(response);

        assertThat(service.createLesson(creator, 10, request)).isEqualTo(response);
        assertThat(section.getLessons()).containsExactly(lesson);
        assertThat(lesson.getSection()).isEqualTo(section);
        verify(repository).save(section);
    }

    @Test
    void shouldReturn404WhenSectionDoesNotExist() {
        when(repository.findByIdAndCourseCreatorId(10, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLesson(creator, 10, new LessonRequest("https://example.com/lesson", 120, 1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldUpdateSectionSuccessfullyWhenCreatorHasAccess() {
        SectionRequest request = new SectionRequest("Updated", 2);
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(repository.save(section)).thenReturn(section);
        when(mapper.toDto(section)).thenReturn(new SectionResponse(10, "Updated", 2, List.of()));

        SectionResponse result = service.update(10, request);

        assertThat(section.getTitle()).isEqualTo("Updated");
        assertThat(section.getPosition()).isEqualTo(2);
        assertThat(result.title()).isEqualTo("Updated");
    }

    @Test
    void shouldReturn403WhenAnotherUserUpdatesSection() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 2)).thenReturn(false);

        assertThatThrownBy(() -> service.update(10, 2, new SectionRequest("Updated", 2)))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("You are not the creator of this course.");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteLessonSuccessfully() {
        Lesson lesson = Lesson.builder().id(30).section(section).build();
        section.setLessons(new ArrayList<>(List.of(lesson)));
        when(repository.findByIdAndCourseCreatorId(10, 1)).thenReturn(Optional.of(section));

        service.deleteLesson(creator, 10, 30);

        assertThat(section.getLessons()).isEmpty();
        verify(repository).save(section);
    }

    @Test
    void shouldReturn404WhenLessonDoesNotExist() {
        section.setLessons(new ArrayList<>());
        when(repository.findByIdAndCourseCreatorId(10, 1)).thenReturn(Optional.of(section));

        assertThatThrownBy(() -> service.deleteLesson(creator, 10, 30))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Lesson not found.");
    }

    @Test
    void shouldDeleteSectionSuccessfully() {
        when(repository.findByIdAndCourseCreatorId(10, 1)).thenReturn(Optional.of(section));

        service.delete(10, 1);

        verify(repository).delete(section);
    }

    @Test
    void shouldReturnLessonSuccessfullyForCreator() {
        Lesson lesson = Lesson.builder().id(30).section(section).contentUrl("https://example.com/lesson").position(1).build();
        LessonResponse response = new LessonResponse(30, lesson.getContentUrl(), 1, null);
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseGateway.findById(1, 20)).thenReturn(new CourseInfo(20, 1, "Java", BigDecimal.TEN, org.example.learnhub.course.entity.CourseStatus.PUBLIC));
        when(enrollmentGateway.existsByUserIdAndCourseId(1, 20)).thenReturn(false);
        when(lessonMapper.toDto(lesson)).thenReturn(response);

        assertThat(service.findLessonById(creator, 30)).isEqualTo(response);
    }

    @Test
    void shouldReturnLessonSuccessfullyForEnrolledStudent() {
        Lesson lesson = Lesson.builder().id(30).section(section).contentUrl("https://example.com/lesson").position(1).build();
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseGateway.findById(2, 20)).thenReturn(new CourseInfo(20, 1, "Java", BigDecimal.TEN, org.example.learnhub.course.entity.CourseStatus.PUBLIC));
        when(enrollmentGateway.existsByUserIdAndCourseId(2, 20)).thenReturn(true);
        when(lessonMapper.toDto(lesson)).thenReturn(new LessonResponse(30, lesson.getContentUrl(), 1, null));

        assertThat(service.findLessonById(student, 30).id()).isEqualTo(30);
    }

    @Test
    void shouldReturn403WhenStudentIsNotEnrolled() {
        Lesson lesson = Lesson.builder().id(30).section(section).contentUrl("https://example.com/lesson").position(1).build();
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseGateway.findById(2, 20)).thenReturn(new CourseInfo(20, 1, "Java", BigDecimal.TEN, org.example.learnhub.course.entity.CourseStatus.PUBLIC));
        when(enrollmentGateway.existsByUserIdAndCourseId(2, 20)).thenReturn(false);

        assertThatThrownBy(() -> service.findLessonById(student, 30))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("User does not have access to this course.");
    }

    @Test
    void shouldReturnSectionLessonsSuccessfullyForEnrolledStudent() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.findById(2, 20)).thenReturn(new CourseInfo(20, 1, "Java", BigDecimal.TEN, org.example.learnhub.course.entity.CourseStatus.PUBLIC));
        when(enrollmentGateway.findByUserIdAndCourseId(2, 20)).thenReturn(Optional.of(new org.example.learnhub.gateway.dto.EnrollmentInfo(1, 2, 20, LocalDateTime.now())));
        Lesson lesson = Lesson.builder().id(30).section(section).contentUrl("https://example.com/lesson").position(1).build();
        when(lessonRepository.findAllBySectionId(10)).thenReturn(List.of(lesson));
        when(lessonMapper.toDto(lesson)).thenReturn(new LessonResponse(30, lesson.getContentUrl(), 1, null));

        assertThat(service.findSectionLessons(student, 10)).hasSize(1);
    }

    @Test
    void shouldReturnLessonEntitySuccessfully() {
        Lesson lesson = Lesson.builder().id(30).build();
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));

        assertThat(service.findLessonEntityById(30)).isEqualTo(lesson);
    }

    @Test
    void shouldReturn404WhenLessonEntityDoesNotExist() {
        when(lessonRepository.findById(30)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findLessonEntityById(30))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Lesson not found.");
    }

    @Test
    void shouldReturnSectionLessonsSuccessfullyForCreator() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.findById(1, 20)).thenReturn(new CourseInfo(20, 1, "Java", BigDecimal.TEN, org.example.learnhub.course.entity.CourseStatus.PUBLIC));
        when(enrollmentGateway.findByUserIdAndCourseId(1, 20)).thenReturn(Optional.empty());
        when(lessonRepository.findAllBySectionId(10)).thenReturn(List.of());

        assertThat(service.findSectionLessons(creator, 10)).isEmpty();
    }

    @Test
    void shouldReturn403WhenStudentIsNotEnrolledInCourse() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.findById(2, 20)).thenReturn(new CourseInfo(20, 1, "Java", BigDecimal.TEN, org.example.learnhub.course.entity.CourseStatus.PUBLIC));
        when(enrollmentGateway.findByUserIdAndCourseId(2, 20)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findSectionLessons(student, 10))
                .isInstanceOf(CourseAccessDeniedException.class);
    }

    @Test
    void shouldCalculateCourseDurationSuccessfully() {
        when(repository.calculateDurationByCourseId(20)).thenReturn(3600);

        assertThat(service.calculateDurationByCourseId(20)).isEqualTo(3600);
    }

    @Test
    void shouldReturn400WhenSectionExceedsTwentyLessons() {
        Section full = Section.builder().id(10).courseId(20).lessons(new ArrayList<>()).build();
        for (int i = 0; i < 20; i++) full.addLesson(Lesson.builder().id(i + 1).build());

        assertThatThrownBy(() -> full.addLesson(Lesson.builder().id(21).build()))
                .isInstanceOf(MaxLessonsReachedException.class);
    }
}
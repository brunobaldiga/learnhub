package org.example.learnhub.section;

import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.exception.CourseAccessDeniedException;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.gateway.CourseGateway;
import org.example.learnhub.gateway.EnrollmentGateway;
import org.example.learnhub.gateway.dto.CourseInfo;
import org.example.learnhub.gateway.dto.EnrollmentInfo;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {
    @Mock
    SectionRepository repository;
    @Mock
    LessonRepository lessonRepository;
    @Mock
    SectionMapper mapper;
    @Mock
    LessonMapper lessonMapper;
    @Mock
    EnrollmentGateway enrollmentGateway;
    @Mock
    CourseGateway courseGateway;

    @InjectMocks
    SectionService service;

    private User creator;
    private User student;
    private Section section;
    private CourseInfo courseInfo;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(1).username("creator").build();
        student = User.builder().id(2).username("student").build();
        section = Section.builder()
                .id(10)
                .title("Introduction")
                .position(1)
                .courseId(20)
                .lessons(new ArrayList<>())
                .build();
        courseInfo = new CourseInfo(20, 1, "Java", BigDecimal.TEN, CurrencyCode.USD, CourseStatus.PUBLIC);
    }

    @Test
    void shouldCreateLessonForCourseCreator() {
        LessonRequest request = new LessonRequest("https://example.com/lesson", 120, 1);
        Lesson lesson = Lesson.builder().id(30).contentUrl(request.contentUrl()).duration(120).position(1).build();
        SectionResponse response = new SectionResponse(
                10, "Introduction", 1,
                List.of(new LessonResponse(30, request.contentUrl(), 120, 1, null))
        );

        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 1)).thenReturn(true);
        when(lessonMapper.toLesson(request)).thenReturn(lesson);
        when(mapper.toDto(section)).thenReturn(response);

        assertThat(service.createLesson(creator, 10, request)).isEqualTo(response);
        assertThat(section.getLessons()).containsExactly(lesson);
        assertThat(lesson.getSection()).isSameAs(section);
        verify(repository).save(section);
    }

    @Test
    void shouldRejectLessonCreationWhenUserDoesNotOwnCourse() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 2)).thenReturn(false);

        assertThatThrownBy(() -> service.createLesson(
                student, 10, new LessonRequest("https://example.com/lesson", 120, 1)))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("You do not own this course.");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCreatingLessonInMissingSection() {
        when(repository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLesson(
                creator, 10, new LessonRequest("https://example.com/lesson", 120, 1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldDeleteLesson() {
        Lesson lesson = Lesson.builder().id(30).section(section).build();
        section.setLessons(new ArrayList<>(List.of(lesson)));
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 1)).thenReturn(true);

        service.deleteLesson(creator, 10, 30);

        assertThat(section.getLessons()).isEmpty();
        verify(repository).save(section);
    }

    @Test
    void shouldThrowWhenDeletingMissingLesson() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteLesson(creator, 10, 30))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Lesson not found.");
    }

    @Test
    void shouldFindLessonForCourseCreator() {
        Lesson lesson = Lesson.builder().id(30).section(section).contentUrl("https://example.com/lesson").duration(120).position(1).build();
        LessonResponse response = new LessonResponse(30, lesson.getContentUrl(), 120, 1, null);
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseGateway.findById(20)).thenReturn(courseInfo);
        when(enrollmentGateway.existsByUserIdAndCourseId(1, 20)).thenReturn(false);
        when(lessonMapper.toDto(lesson)).thenReturn(response);

        assertThat(service.findLessonById(creator, 30)).isEqualTo(response);
    }

    @Test
    void shouldFindLessonForEnrolledStudent() {
        Lesson lesson = Lesson.builder().id(30).section(section).duration(120).position(1).build();
        CourseInfo info = new CourseInfo(20, 1, "Java", BigDecimal.TEN, CurrencyCode.USD, CourseStatus.PUBLIC);
        LessonResponse response = new LessonResponse(30, "https://example.com/lesson", 120, 1, null);
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseGateway.findById(20)).thenReturn(info);
        when(enrollmentGateway.existsByUserIdAndCourseId(2, 20)).thenReturn(true);
        when(lessonMapper.toDto(lesson)).thenReturn(response);

        assertThat(service.findLessonById(student, 30)).isEqualTo(response);
    }

    @Test
    void shouldRejectLessonAccessForNonEnrolledStudent() {
        Lesson lesson = Lesson.builder().id(30).section(section).build();
        when(lessonRepository.findById(30)).thenReturn(Optional.of(lesson));
        when(courseGateway.findById(20)).thenReturn(courseInfo);
        when(enrollmentGateway.existsByUserIdAndCourseId(2, 20)).thenReturn(false);

        assertThatThrownBy(() -> service.findLessonById(student, 30))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("User does not have access to this course.");
    }

    @Test
    void shouldThrowWhenLessonDoesNotExist() {
        when(lessonRepository.findById(30)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findLessonEntityById(30))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Lesson not found.");
    }

    @Test
    void shouldListSectionLessonsForCreator() {
        Lesson lesson = Lesson.builder().id(30).section(section).build();
        LessonResponse response = new LessonResponse(30, "https://example.com/lesson", 120, 1, null);
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.findById(20)).thenReturn(courseInfo);
        when(enrollmentGateway.findByUserIdAndCourseId(1, 20)).thenReturn(Optional.empty());
        when(lessonRepository.findAllBySectionId(10)).thenReturn(List.of(lesson));
        when(lessonMapper.toDto(lesson)).thenReturn(response);

        assertThat(service.findSectionLessons(creator, 10)).containsExactly(response);
    }

    @Test
    void shouldListSectionLessonsForEnrolledStudent() {
        Lesson lesson = Lesson.builder().id(30).section(section).build();
        LessonResponse response = new LessonResponse(30, "https://example.com/lesson", 120, 1, null);
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.findById(20)).thenReturn(courseInfo);
        when(enrollmentGateway.findByUserIdAndCourseId(2, 20))
                .thenReturn(Optional.of(new EnrollmentInfo(5, 2, 20, LocalDateTime.now())));
        when(lessonRepository.findAllBySectionId(10)).thenReturn(List.of(lesson));
        when(lessonMapper.toDto(lesson)).thenReturn(response);

        assertThat(service.findSectionLessons(student, 10)).containsExactly(response);
    }

    @Test
    void shouldRejectListingLessonsForNonEnrolledStudent() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.findById(20)).thenReturn(courseInfo);
        when(enrollmentGateway.findByUserIdAndCourseId(2, 20)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findSectionLessons(student, 10))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("User does not have access to this course.");
    }

    @Test
    void shouldFindSectionOwnedByCreator() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 1)).thenReturn(true);

        assertThat(service.findSectionEntityByIdAndCourseCreatorId(10, 1)).isSameAs(section);
    }

    @Test
    void shouldRejectSectionWhenCreatorDoesNotOwnCourse() {
        when(repository.findById(10)).thenReturn(Optional.of(section));
        when(courseGateway.isCourseCreator(20, 2)).thenReturn(false);

        assertThatThrownBy(() -> service.findSectionEntityByIdAndCourseCreatorId(10, 2))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessage("You do not own this course.");
    }

    @Test
    void shouldCalculateCourseDuration() {
        when(repository.calculateDurationByCourseId(20)).thenReturn(3600);
        assertThat(service.calculateDurationByCourseId(20)).isEqualTo(3600);
    }
}

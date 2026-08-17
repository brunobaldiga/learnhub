package org.example.learnhub.section;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.section.dto.LessonRequest;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.entity.Section;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SectionServiceTest {
    @Mock
    private SectionRepository repository;

    @Mock
    private SectionMapper mapper;

    @Mock
    private LessonMapper lessonMapper;

    @InjectMocks
    private SectionService service;

    private User user;
    private Course course;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("John").build();
        course = Course.builder().id(1).build();
    }

    @Test
    void shouldCreateLessonSuccessfully() {
        Section section = Section.builder().id(1).lessons(new ArrayList<>()).build();

        LessonRequest request = new LessonRequest("https://youtube.com/video", 120, 1);

        Lesson lesson = Lesson.builder()
                .id(1)
                .contentUrl(request.contentUrl())
                .position(request.position())
                .duration(request.duration())
                .build();

        SectionResponse response = new SectionResponse(
                1,
                "Section 1",
                1,
                List.of(
                        new LessonResponse(
                                1,
                                request.contentUrl(),
                                0,
                                LocalDateTime.now()
                        )
                )
        );

        when(repository.findByIdAndCourseCreatorId(1, user.getId())).thenReturn(Optional.of(section));
        when(lessonMapper.toLesson(request)).thenReturn(lesson);
        when(mapper.toDto(section)).thenReturn(response);

        SectionResponse result = service.createLesson(user, 1, request);

        verify(repository).save(section);

        assertThat(result).isEqualTo(response);
        assertThat(section.getLessons().size()).isEqualTo(1);
    }

    @Test
    void shouldReturn404WhenSectionNotFoundOnCreateLesson() {
        LessonRequest request = new LessonRequest("https://youtube.com/video", 120, 1);

        when(repository.findByIdAndCourseCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLesson(user, 1, request))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldDeleteLessonSuccessfully() {
        Lesson lesson = Lesson.builder().id(1).build();

        Section section = Section.builder().id(1).lessons(new ArrayList<>(List.of(lesson))).build();

        when(repository.findByIdAndCourseCreatorId(1, user.getId())).thenReturn(Optional.of(section));

        service.deleteLesson(user, 1, 1);

        verify(repository).save(section);
        assertThat(section.getLessons().isEmpty()).isTrue();
    }

    @Test
    void shouldReturn404WhenSectionNotFoundOnDeleteLesson() {
        when(repository.findByIdAndCourseCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteLesson(user, 1, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldSaveSectionSuccessfully() {
        Section section = Section.builder().id(1).build();

        when(repository.save(section)).thenReturn(section);

        Section result = service.saveSection(section);

        assertThat(result).isEqualTo(section);

        verify(repository).save(section);
    }

    @Test
    void shouldCreateSectionSuccessfully() {
        SectionRequest request = new SectionRequest("Section 1", 0);

        Section section = Section.builder().id(1).course(course).build();

        when(mapper.toSection(request, course)).thenReturn(section);
        when(repository.save(section)).thenReturn(section);

        Section result = service.createSection(request, course);

        assertThat(result).isEqualTo(section);

        verify(mapper).toSection(request, course);
        verify(repository).save(section);
    }

    @Test
    void shouldReturnSectionSuccessfully() {
        Section section = Section.builder().id(1).course(course).build();

        when(repository.findByIdAndCourseCreatorId(1, user.getId())).thenReturn(Optional.of(section));

        Section result = service.findSectionEntityByIdAndCourseCreatorId(1, user.getId());

        assertThat(result).isEqualTo(section);

        verify(repository).findByIdAndCourseCreatorId(1, user.getId());
    }

    @Test
    void shouldReturn404WhenSectionDoesNotExist() {
        when(repository.findByIdAndCourseCreatorId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.findSectionEntityByIdAndCourseCreatorId(1, user.getId()))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldDeleteSectionSuccessfully() {
        Section section = Section.builder().id(1).build();

        service.deleteSection(section);

        verify(repository).delete(section);
    }
}
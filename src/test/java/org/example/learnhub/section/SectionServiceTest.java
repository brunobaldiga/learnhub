package org.example.learnhub.section;

import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.dto.LessonRequest;
import org.example.learnhub.section.dto.LessonResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.entity.Lesson;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.section.service.LessonMapper;
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

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .username("John")
                .build();
    }

    @Test
    void shouldCreateLessonSuccessfully() {
        Section section = Section.builder()
                .id(1)
                .lessons(new ArrayList<>())
                .build();

        LessonRequest request = new LessonRequest(
                "https://youtube.com/video",
                0
        );

        Lesson lesson = Lesson.builder()
                .id(1)
                .contentUrl(request.contentUrl())
                .index(request.index())
                .build();

        SectionResponse response = new SectionResponse(
                1,
                "Section 1",
                0,
                List.of(
                        new LessonResponse(
                                1,
                                request.contentUrl(),
                                0,
                                LocalDateTime.now()
                        )
                )
        );

        when(repository.findByIdAndCourseCreatorId(1, user.getId()))
                .thenReturn(Optional.of(section));

        when(lessonMapper.toLesson(request)).thenReturn(lesson);
        when(mapper.toDto(section)).thenReturn(response);

        SectionResponse result = service.createLesson(user, 1, request);

        verify(repository).save(section);

        assertThat(result).isEqualTo(response);
        assertThat(section.getLessons().size()).isEqualTo(1);
    }

    @Test
    void shouldReturn404WhenSectionNotFoundOnCreateLesson() {
        LessonRequest request = new LessonRequest(
                "https://youtube.com/video",
                0
        );

        when(repository.findByIdAndCourseCreatorId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLesson(user, 1, request))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldDeleteLessonSuccessfully() {
        Lesson lesson = Lesson.builder()
                .id(1)
                .build();

        Section section = Section.builder()
                .id(1)
                .lessons(new ArrayList<>(List.of(lesson)))
                .build();


        when(repository.findByIdAndCourseCreatorId(1, user.getId())).thenReturn(Optional.of(section));

        service.deleteLesson(user, 1, 1);

        verify(repository).save(section);
        assertThat(section.getLessons().isEmpty()).isTrue();
    }

    @Test
    void shouldReturn404WhenSectionNotFoundOnDeleteLesson() {
        when(repository.findByIdAndCourseCreatorId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteLesson(user, 1, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }
}
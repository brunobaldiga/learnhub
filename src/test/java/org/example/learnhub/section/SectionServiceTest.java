package org.example.learnhub.section;

import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.dto.VideoRequest;
import org.example.learnhub.section.dto.VideoResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.entity.Video;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.section.service.VideoMapper;
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
    private VideoMapper videoMapper;

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
    void shouldCreateVideoSuccessfully() {
        Section section = Section.builder()
                .id(1)
                .videos(new ArrayList<>())
                .build();

        VideoRequest request = new VideoRequest(
                "https://youtube.com/video",
                0
        );

        Video video = Video.builder()
                .id(1)
                .videoUrl(request.videoUrl())
                .index(request.index())
                .build();

        SectionResponse response = new SectionResponse(
                1,
                "Section 1",
                0,
                List.of(
                        new VideoResponse(
                                1,
                                request.videoUrl(),
                                0,
                                LocalDateTime.now()
                        )
                )
        );

        when(repository.findByIdAndCourseCreatorId(1, user.getId()))
                .thenReturn(Optional.of(section));

        when(videoMapper.toVideo(request)).thenReturn(video);
        when(mapper.toDto(section)).thenReturn(response);

        SectionResponse result = service.create(user, 1, request);

        verify(repository).save(section);

        assertThat(result).isEqualTo(response);
        assertThat(section.getVideos().size()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenSectionNotFoundOnCreateVideo() {
        VideoRequest request = new VideoRequest(
                "https://youtube.com/video",
                0
        );

        when(repository.findByIdAndCourseCreatorId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(user, 1, request))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }

    @Test
    void shouldDeleteVideoSuccessfully() {
        Video video = Video.builder()
                .id(1)
                .build();

        Section section = Section.builder()
                .id(1)
                .videos(new ArrayList<>(List.of(video)))
                .build();

        SectionResponse response = new SectionResponse(
                1,
                "Section 1",
                0,
                List.of()
        );

        when(repository.findByIdAndCourseCreatorId(1, user.getId()))
                .thenReturn(Optional.of(section));

        when(mapper.toDto(section)).thenReturn(response);

        SectionResponse result = service.delete(user, 1, 1);

        verify(repository).save(section);

        assertThat(result).isEqualTo(response);
        assertThat(section.getVideos().isEmpty()).isTrue();
    }

    @Test
    void shouldThrowWhenSectionNotFoundOnDeleteVideo() {
        when(repository.findByIdAndCourseCreatorId(any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(user, 1, 1))
                .isInstanceOf(EntityNotFound.class)
                .hasMessage("Section not found");
    }
}
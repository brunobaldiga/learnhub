package org.example.learnhub.section;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.exception.EntityNotFoundException;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.infra.SectionGatewayImpl;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.section.service.SectionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionGatewayImplTest {
    @Mock
    private SectionMapper mapper;

    @Mock
    private SectionRepository repository;

    @InjectMocks
    private SectionGatewayImpl gateway;

    @Test
    void shouldCreateSection() {
        SectionRequest request = new SectionRequest("Introduction", 1);
        Section section = Section.builder().id(3).title("Introduction").position(1).courseId(10).build();
        SectionInfo info = new SectionInfo(3, "Introduction", 1, 10);
        when(mapper.toSection(request, 10)).thenReturn(section);
        when(repository.save(section)).thenReturn(section);
        when(mapper.toSectionInfo(section)).thenReturn(info);

        assertThat(gateway.create(request, 10)).isEqualTo(info);
    }

    @Test
    void shouldUpdateSection() {
        SectionRequest request = new SectionRequest("Advanced Java", 2);
        Section section = Section.builder().id(3).title("Introduction").position(1).courseId(10).build();
        SectionResponse response = new SectionResponse(3, "Advanced Java", 2, List.of());
        when(repository.findById(3)).thenReturn(Optional.of(section));
        when(repository.save(section)).thenReturn(section);
        when(mapper.toDto(section)).thenReturn(response);

        SectionResponse result = gateway.update(3, request);

        assertThat(result).isEqualTo(response);
        assertThat(section.getTitle()).isEqualTo("Advanced Java");
        assertThat(section.getPosition()).isEqualTo(2);
    }

    @Test
    void shouldThrowWhenUpdatingMissingSection() {
        when(repository.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateway.update(3, new SectionRequest("Advanced Java", 2)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Section not found.");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldDeleteSection() {
        Section section = Section.builder().id(3).build();
        when(repository.findById(3)).thenReturn(Optional.of(section));

        gateway.delete(3);

        verify(repository).delete(section);
    }

    @Test
    void shouldThrowWhenDeletingMissingSection() {
        when(repository.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateway.delete(3))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Section not found");
        verify(repository, never()).delete(any());
    }

    @Test
    void shouldDelegateSectionCount() {
        when(repository.countByCourseId(10)).thenReturn(4L);

        assertThat(gateway.countSectionsByCourseId(10)).isEqualTo(4L);
    }

    @Test
    void shouldReturnSectionsInRepositoryOrder() {
        Section first = Section.builder().id(1).build();
        Section second = Section.builder().id(2).build();
        SectionResponse firstResponse = new SectionResponse(1, "First", 1, List.of());
        SectionResponse secondResponse = new SectionResponse(2, "Second", 2, List.of());
        when(repository.findAllByCourseIdOrderByPositionAsc(10)).thenReturn(List.of(first, second));
        when(mapper.toDto(first)).thenReturn(firstResponse);
        when(mapper.toDto(second)).thenReturn(secondResponse);

        assertThat(gateway.findAllByCourseId(10)).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void shouldFindSectionInfoById() {
        Section section = Section.builder().id(3).build();
        SectionInfo info = new SectionInfo(3, "Introduction", 1, 10);
        when(repository.findById(3)).thenReturn(Optional.of(section));
        when(mapper.toSectionInfo(section)).thenReturn(info);

        assertThat(gateway.findById(3)).isEqualTo(info);
    }

    @Test
    void shouldThrowWhenFindingMissingSection() {
        when(repository.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateway.findById(3))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Section not found.");
    }
}

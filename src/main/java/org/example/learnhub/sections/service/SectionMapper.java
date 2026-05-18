package org.example.learnhub.sections.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.sections.dto.SectionResponse;
import org.example.learnhub.sections.entity.Section;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionMapper {
    private final VideoMapper videoMapper;

    public Section toSection(SectionRequest request, Course course) {
        return Section.builder()
                .title(request.title())
                .index(request.index())
                .course(course)
                .build();
    }

    public SectionResponse toDto(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getIndex(),
                section.getVideos().stream().map(videoMapper::toDto).toList()
        );
    }
}

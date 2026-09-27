package org.example.learnhub.section.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionMapper {
    private final LessonMapper lessonMapper;

    public Section toSection(SectionRequest request, Integer courseId) {
        return Section.builder()
                .title(request.title())
                .position(request.position())
                .courseId(courseId)
                .build();
    }

    public SectionResponse toDto(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getPosition(),
                section.getLessons().stream().map(lessonMapper::toDto).toList()
        );
    }

    public SectionInfo toSectionInfo(Section section) {
        return new SectionInfo(
                section.getId(),
                section.getTitle(),
                section.getPosition(),
                section.getCourseId()
        );
    }
}

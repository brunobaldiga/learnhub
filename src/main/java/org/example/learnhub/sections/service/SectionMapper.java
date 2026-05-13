package org.example.learnhub.sections.service;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.sections.dto.SectionResponse;
import org.example.learnhub.sections.dto.VideoResponse;
import org.example.learnhub.sections.entity.Section;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionMapper {

    public Section toSection(SectionRequest request, Course course) {
        return Section.builder()
                .title(request.title())
                .index(request.index())
                .course(course)
                .build();
    }

    public SectionResponse toDto(Section section, List<VideoResponse> videos) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getIndex(),
                videos
        );
    }
}

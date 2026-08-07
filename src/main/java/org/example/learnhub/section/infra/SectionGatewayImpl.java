package org.example.learnhub.section.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.service.SectionMapper;
import org.example.learnhub.section.service.SectionService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionGatewayImpl implements SectionGateway {
    private final SectionService service;
    private final SectionMapper mapper;

    @Override
    public void deleteSection(Section section) {
        service.deleteSection(section);
    }

    @Override
    public Section saveSection(Section section) {
        return service.saveSection(section);
    }

    @Override
    public Section createSection(SectionRequest request, Course course) {
        return service.createSection(request, course);
    }

    @Override
    public SectionResponse toDto(Section section) {
        return mapper.toDto(section);
    }

    @Override
    public Section findByIdAndCourseCreatorId(Integer sectionId, Integer creatorId) {
        return service.findSectionEntityByIdAndCourseCreatorId(sectionId, creatorId);
    }
}

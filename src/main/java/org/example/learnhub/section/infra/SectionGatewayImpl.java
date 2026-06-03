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
    public Section saveSection(SectionRequest request, Course course) {
        return service.save(request, course);
    }

    @Override
    public SectionResponse toDto(Section section) {
        return mapper.toDto(section);
    }
}

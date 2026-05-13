package org.example.learnhub.sections.gateway;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.course.gateway.SectionGateway;
import org.example.learnhub.sections.entity.Section;
import org.example.learnhub.sections.service.SectionService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionGatewayImpl implements SectionGateway {
    private final SectionService service;

    @Override
    public Section saveSection(SectionRequest request, Course course) {
        return service.save(request, course);
    }
}

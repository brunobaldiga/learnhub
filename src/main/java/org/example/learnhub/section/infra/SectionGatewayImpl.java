package org.example.learnhub.section.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.service.SectionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SectionGatewayImpl implements SectionGateway {
    private final SectionService service;

    @Override
    public SectionInfo create(SectionRequest request, Integer courseId) {
        return service.create(request, courseId);
    }

    @Override
    public SectionResponse update(Integer sectionId, Integer creatorId, SectionRequest request) {
        return service.update(sectionId, creatorId, request);
    }

    @Override
    public void delete(Integer sectionId, Integer creatorId) {
        service.delete(sectionId, creatorId);
    }

    @Override
    public long countSectionsByCourseId(Integer courseId) {
        return service.countSectionsByCourseId(courseId);
    }

    @Override
    public List<SectionResponse> findAllByCourseId(Integer courseId) {
        return service.findAllByCourseId(courseId);
    }
}

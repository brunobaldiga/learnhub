package org.example.learnhub.section.infra;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.gateway.SectionGateway;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.service.SectionService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SectionGatewayImpl implements SectionGateway {
    private final SectionService service;

    @Override
    public SectionInfo create(SectionRequest request, Integer courseId) {
        return service.createSection(request, courseId);
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
    public Integer countSectionsByCourseId(Integer courseId) {
        return 0;
    }

    @Override
    public List<SectionResponse> findAllByCourseId(Integer courseId) {
        return List.of();
    }

    @Override
    public SectionInfo findByIdAndCourseCreatorId(Integer sectionId, Integer creatorId) {
        Section section = service.findSectionEntityByIdAndCourseCreatorId(sectionId, creatorId);

        return new SectionInfo(
                section.getId(),
                section.getTitle(),
                section.getPosition(),
                section.getCourseId()
        );
    }
}

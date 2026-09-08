package org.example.learnhub.gateway;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;

import java.util.List;

public interface SectionGateway {
    SectionInfo create(SectionRequest request, Integer courseId);

    SectionResponse update(Integer sectionId, Integer creatorId, SectionRequest request);

    void delete(Integer sectionId, Integer creatorId);

    long countSectionsByCourseId(Integer courseId);

    List<SectionResponse> findAllByCourseId(Integer courseId);
}

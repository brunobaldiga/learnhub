package org.example.learnhub.gateway;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.gateway.dto.SectionInfo;
import org.example.learnhub.section.dto.SectionResponse;

import java.util.List;

public interface SectionGateway {
    SectionInfo createSection(SectionRequest request, Integer courseId);

    SectionResponse updateSection(Integer sectionId, Integer creatorId, SectionRequest request);

    SectionInfo findByIdAndCourseCreatorId(Integer sectionId, Integer creatorId);

    void deleteSection(Integer sectionId, Integer creatorId);

    Integer countSectionsByCourseId(Integer courseId);

    List<SectionResponse> findAllByCourseId(Integer courseId);
}

package org.example.learnhub.course.gateway;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.sections.dto.SectionResponse;
import org.example.learnhub.sections.entity.Section;

import java.util.List;

public interface SectionGateway {
    Section saveSection(SectionRequest request, Course course);

    SectionResponse toDto(Section section);

    List<SectionResponse> findAllByCourseId(Integer courseId);
}

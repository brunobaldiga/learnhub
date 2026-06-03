package org.example.learnhub.gateway;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;

public interface SectionGateway {
    Section saveSection(SectionRequest request, Course course);

    SectionResponse toDto(Section section);
}

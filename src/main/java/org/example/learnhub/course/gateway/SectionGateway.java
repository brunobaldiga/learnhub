package org.example.learnhub.course.gateway;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.sections.entity.Section;

public interface SectionGateway {
    Section saveSection(SectionRequest request, Course course);
}

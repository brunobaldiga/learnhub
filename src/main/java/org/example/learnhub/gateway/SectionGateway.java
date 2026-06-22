package org.example.learnhub.gateway;

import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.user.entity.User;

public interface SectionGateway {
    Section saveSection(Section section);

    Section createSection(SectionRequest request, Course course);

    SectionResponse toDto(Section section);

    Section findByIdAndCourseCreatorId(Integer sectionId, Integer creatorId);

    void deleteSection(Section section);
}

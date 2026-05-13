package org.example.learnhub.course.dto;

import org.example.learnhub.course.entity.CourseStatus;
import org.example.learnhub.course.entity.Section;

import java.math.BigDecimal;
import java.util.List;

public record UpdateCourseRequest(
        String title,
        CourseStatus status,
        BigDecimal price,
        Integer salesAmount,
        List<Section> sections
) {}

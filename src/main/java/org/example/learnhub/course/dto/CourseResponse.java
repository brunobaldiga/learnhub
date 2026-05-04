package org.example.learnhub.course.dto;

import org.example.learnhub.course.entity.Section;
import org.example.learnhub.course.entity.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse (
    Integer id,
    Integer creatorId,
    String creatorUsername,
    String title,
    CourseStatus status,
    List<Section> sections,
    LocalDateTime createdAt
) {}

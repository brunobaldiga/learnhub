package org.example.learnhub.gateway.dto;

public record SectionInfo(
        Integer id,
        String title,
        Integer position,
        Integer courseId
) {
}

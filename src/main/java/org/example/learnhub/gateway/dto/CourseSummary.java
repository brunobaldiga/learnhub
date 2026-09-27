package org.example.learnhub.gateway.dto;

public record CourseSummary(
        Integer id,
        String title,
        String creatorUsername
) {
}

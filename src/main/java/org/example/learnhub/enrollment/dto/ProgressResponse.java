package org.example.learnhub.enrollment.dto;

public record ProgressResponse(
        Integer completedLessons,
        Integer totalLessons,
        Double completedPercentage,
        Boolean courseCompleted
) {}

package org.example.learnhub.progress.dto;

public record ProgressResponse(
        Boolean completed,
        Integer completedLessons,
        Integer totalLessons,
        Double completedPercentage,
        Boolean courseCompleted
) {}

package org.example.learnhub.lesson.dto;

public record ProgressResponse(
        Boolean completed,
        Integer completedLessons,
        Integer totalLessons,
        Double completedPercentage,
        Boolean courseCompleted
) {}

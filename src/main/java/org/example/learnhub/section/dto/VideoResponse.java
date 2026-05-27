package org.example.learnhub.section.dto;

import java.time.LocalDateTime;

public record VideoResponse(
        Integer id,
        String videoUrl,
        Integer index,
        LocalDateTime createdAt
) {}

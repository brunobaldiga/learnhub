package org.example.learnhub.sections.dto;

import java.util.List;

public record SectionResponse(
        Integer id,
        String title,
        Integer index,
        List<VideoResponse> videos
) {
}

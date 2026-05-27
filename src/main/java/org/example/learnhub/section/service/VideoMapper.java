package org.example.learnhub.section.service;

import org.example.learnhub.section.dto.VideoRequest;
import org.example.learnhub.section.dto.VideoResponse;
import org.example.learnhub.section.entity.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {
    public Video toVideo(VideoRequest request) {
        return Video.builder()
                .videoUrl(request.videoUrl())
                .index(request.index())
                .build();
    }


    public VideoResponse toDto(Video video) {
        return new VideoResponse(
                video.getId(),
                video.getVideoUrl(),
                video.getIndex(),
                video.getCreatedAt()
        );
    }
}

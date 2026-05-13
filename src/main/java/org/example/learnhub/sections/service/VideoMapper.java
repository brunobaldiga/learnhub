package org.example.learnhub.sections.service;

import org.example.learnhub.sections.dto.VideoRequest;
import org.example.learnhub.sections.dto.VideoResponse;
import org.example.learnhub.sections.entity.Video;
import org.springframework.stereotype.Service;

@Service
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

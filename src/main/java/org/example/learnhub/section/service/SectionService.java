package org.example.learnhub.section.service;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.course.dto.SectionRequest;
import org.example.learnhub.course.entity.Course;
import org.example.learnhub.exception.EntityNotFound;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.dto.VideoRequest;
import org.example.learnhub.section.entity.Section;
import org.example.learnhub.section.entity.Video;
import org.example.learnhub.section.repository.SectionRepository;
import org.example.learnhub.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository repository;
    private final SectionMapper mapper;
    private final VideoMapper videoMapper;

    public Section save(SectionRequest request, Course course) {
        Section section = mapper.toSection(request, course);

        return repository.save(section);
    }

    public SectionResponse create(User user, Integer sectionId, VideoRequest request) {
        Section section = repository.findByIdAndCourseCreatorId(sectionId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Section not found"));

        Video video = videoMapper.toVideo(request);
        video.setSection(section);

        section.getVideos().add(video);

        repository.save(section);


        return mapper.toDto(section);
    }

    public SectionResponse delete(User user, Integer sectionId, Integer videoId) {
        Section section = repository.findByIdAndCourseCreatorId(sectionId, user.getId())
                .orElseThrow(() -> new EntityNotFound("Section not found"));

        section.getVideos().removeIf(video -> video.getId().equals(videoId));

        repository.save(section);

        return mapper.toDto(section);
    }
}

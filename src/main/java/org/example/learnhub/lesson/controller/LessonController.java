package org.example.learnhub.lesson.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.lesson.dto.ProgressRequest;
import org.example.learnhub.lesson.dto.ProgressResponse;
import org.example.learnhub.lesson.service.LessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService service;

    @PatchMapping("/{lessonId}/progress")
    public ResponseEntity<ProgressResponse> progress(
            @RequestBody ProgressRequest request,
            @PathVariable Integer lessonId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.progress(lessonId, request));
    }
}

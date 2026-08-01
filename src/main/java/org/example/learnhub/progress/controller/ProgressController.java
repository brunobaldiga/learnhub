package org.example.learnhub.progress.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.progress.dto.ProgressRequest;
import org.example.learnhub.progress.dto.ProgressResponse;
import org.example.learnhub.progress.service.ProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lesson")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService service;

    @PatchMapping("/{lessonId}/progress")
    public ResponseEntity<ProgressResponse> progress(
            @RequestBody ProgressRequest request,
            @PathVariable Integer lessonId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.progress(lessonId, request));
    }
}

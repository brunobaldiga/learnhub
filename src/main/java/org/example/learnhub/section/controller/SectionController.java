package org.example.learnhub.section.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.section.dto.SectionResponse;
import org.example.learnhub.section.dto.VideoRequest;
import org.example.learnhub.section.service.SectionService;
import org.example.learnhub.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {
    private final SectionService service;

    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @PostMapping("/{sectionId}/videos")
    public ResponseEntity<SectionResponse> create(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId,
            @RequestBody VideoRequest request
    ) {
        return ResponseEntity.ok(service.create(user, sectionId, request));
    }


    @PreAuthorize("hasAnyRole('CREATOR', 'ADMIN')")
    @DeleteMapping("/{sectionId}/videos/{videoId}")
    public ResponseEntity<SectionResponse> create(
            @AuthenticationPrincipal User user,
            @PathVariable Integer sectionId,
            @PathVariable Integer videoId
    ) {
        return ResponseEntity.ok(service.delete(user, sectionId, videoId));
    }
}

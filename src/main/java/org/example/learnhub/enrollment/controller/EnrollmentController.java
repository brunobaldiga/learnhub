package org.example.learnhub.enrollment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.enrollment.dto.CertificateResponse;
import org.example.learnhub.enrollment.dto.EnrollmentResponse;
import org.example.learnhub.enrollment.dto.ProgressRequest;
import org.example.learnhub.enrollment.dto.ProgressResponse;
import org.example.learnhub.enrollment.service.EnrollmentService;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(
        name = "Enrollments",
        description = "Operations related to course enrollments"
)
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {
    private final EnrollmentService service;

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    @Operation(
            summary = "List user enrollments",
            description = "Returns a paginated list of courses the authenticated user is enrolled in"
    )
    public ResponseEntity<Page<EnrollmentResponse>> findUserEnrollments(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findEnrolledCourses(user.getId(), page, size));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{enrollmentId}")
    @Operation(
            summary = "Get enrollment details",
            description = "Returns details of a specific enrollment belonging to the authenticated user"
    )
    public ResponseEntity<EnrollmentResponse> findEnrollmentById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer enrollmentId
    ) {
        return ResponseEntity.ok(service.findEnrollmentById(user.getId(), enrollmentId));
    }

    @PatchMapping("/lesson/{lessonId}/progress")
    public ResponseEntity<ProgressResponse> progress(
            @RequestBody ProgressRequest request,
            @PathVariable Integer lessonId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.progress(user, lessonId, request));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{enrollmentId}/certificate")
    @Operation(
            summary = "Generate certificate",
            description = "Generate a certificate upon course completion"
    )
    public ResponseEntity<CertificateResponse> generateCertificate(
            @AuthenticationPrincipal User user,
            @PathVariable Integer enrollmentId
    ) {
        CertificateResponse response = service.generateCertificate(user, enrollmentId);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(uri)
                .body(response);
    }

}
package org.example.learnhub.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.entity.User;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(
        name = "Payments",
        description = "Operations related to course purchases and payment history"
)
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{courseId}")
    @Operation(
            summary = "Purchase a course",
            description = "Creates a purchase for the specified course and enrolls the authenticated user"
    )
    public ResponseEntity<PurchaseResponse> purchase(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.purchase(user, courseId));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    @Operation(
            summary = "Get payment history",
            description = "Returns the authenticated user's purchase history with optional date filtering and pagination"
    )
    public ResponseEntity<Page<PurchaseResponse>> history(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(service.history(user, startDate, endDate, pageable));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{paymentId}")
    @Operation(
            summary = "Get payment details",
            description = "Returns details of a specific payment belonging to the authenticated user"
    )
    public ResponseEntity<PurchaseResponse> findById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer paymentId
    ) {
        return ResponseEntity.ok(service.findById(user, paymentId));
    }
}
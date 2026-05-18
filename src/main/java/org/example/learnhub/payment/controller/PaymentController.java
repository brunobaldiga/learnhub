package org.example.learnhub.payment.controller;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.payment.dto.PurchaseResponse;
import org.example.learnhub.payment.service.PaymentService;
import org.example.learnhub.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService service;

    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @PostMapping("/{courseId}")
    public ResponseEntity<PurchaseResponse> purchase(
            @AuthenticationPrincipal User user,
            @PathVariable Integer courseId
    ) {
        return ResponseEntity.ok(service.purchase(user, courseId));
    }


    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping()
    public ResponseEntity<Page<PurchaseResponse>> history(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.history(user, startDate, endDate, pageable));
    }


    @PreAuthorize("hasAnyRole('USER', 'CREATOR', 'ADMIN')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PurchaseResponse> findById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer paymentId
    ) {
        return ResponseEntity.ok(service.findById(user, paymentId));
    }
}

package org.example.learnhub.enrollment.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CertificateResponse(
        UUID id,
        String fullName,
        String courseName,
        Integer courseLengthInHours,
        LocalDate issuedAt
) {
}

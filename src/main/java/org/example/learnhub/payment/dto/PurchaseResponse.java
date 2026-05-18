package org.example.learnhub.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Integer id,
        Integer courseId,
        String courseTitle,
        BigDecimal price,
        LocalDateTime purchasedAt
) {
}

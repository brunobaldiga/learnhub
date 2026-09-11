package org.example.learnhub.payment.dto;

import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Integer id,
        Integer courseId,
        String courseTitle,
        BigDecimal coursePrice,
        CurrencyCode courseCurrency,
        BigDecimal exchangeRate,
        CurrencyCode paidCurrency,
        BigDecimal paidPrice,
        LocalDateTime purchasedAt
) {
}

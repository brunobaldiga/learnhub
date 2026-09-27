package org.example.learnhub.payment.dto;

import jakarta.validation.constraints.NotNull;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;

public record PurchaseRequest(
        @NotNull(message = "Payment currency is required.")
        CurrencyCode currency
) {
}

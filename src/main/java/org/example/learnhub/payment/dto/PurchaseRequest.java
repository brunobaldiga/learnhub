package org.example.learnhub.payment.dto;

import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;

public record PurchaseRequest(
        CurrencyCode currency
) {
}

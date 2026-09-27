package org.example.learnhub.integration.frankfurter;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FrankfurterRateResponse(
        LocalDate date,
        String base,
        String quote,
        BigDecimal rate
) {
}

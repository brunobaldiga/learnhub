package org.example.learnhub.gateway;

import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;

import java.math.BigDecimal;

public interface CurrencyExchangeGateway {
    BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to);
}

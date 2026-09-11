package org.example.learnhub.integration.frankfurter;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.example.learnhub.exception.CurrencyExchangeException;
import org.example.learnhub.gateway.CurrencyExchangeGateway;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class FrankfurterCurrencyExchangeGatewayImpl implements CurrencyExchangeGateway {
    private final FrankfurterClient frankfurterClient;

    @Override
    public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to) {
        if(from.equals(to)) return BigDecimal.ONE;

        try {
            FrankfurterRateResponse response = frankfurterClient.getRate(from.name(), to.name());

            if(response == null || response.rate() == null)
                throw new CurrencyExchangeException("Could not retrieve exchange rate.");

            return response.rate();
        } catch (FeignException exception) {
            throw new CurrencyExchangeException("Currency exchange service is unavaiable.");
        }
    }
}

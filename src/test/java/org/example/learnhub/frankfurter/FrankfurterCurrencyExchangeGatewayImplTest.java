package org.example.learnhub.frankfurter;

import feign.FeignException;
import org.example.learnhub.exception.CurrencyExchangeException;
import org.example.learnhub.integration.frankfurter.FrankfurterClient;
import org.example.learnhub.integration.frankfurter.FrankfurterCurrencyExchangeGatewayImpl;
import org.example.learnhub.integration.frankfurter.FrankfurterRateResponse;
import org.example.learnhub.integration.frankfurter.currency.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FrankfurterCurrencyExchangeGatewayImplTest {
    @Mock
    private FrankfurterClient client;

    @InjectMocks
    private FrankfurterCurrencyExchangeGatewayImpl gateway;

    @Test
    void shouldReturnOneWithoutCallingClientWhenCurrenciesMatch() {
        assertThat(gateway.getExchangeRate(CurrencyCode.USD, CurrencyCode.USD)).isEqualByComparingTo(BigDecimal.ONE);
        verifyNoInteractions(client);
    }

    @Test
    void shouldReturnExchangeRateFromClient() {
        BigDecimal rate = new BigDecimal("5.42");
        when(client.getRate("USD", "BRL"))
                .thenReturn(new FrankfurterRateResponse(LocalDate.of(2026, 9, 15), "USD", "BRL", rate));

        assertThat(gateway.getExchangeRate(CurrencyCode.USD, CurrencyCode.BRL)).isEqualByComparingTo(rate);
    }

    @Test
    void shouldThrowWhenClientReturnsNullResponse() {
        when(client.getRate("USD", "BRL")).thenReturn(null);

        assertThatThrownBy(() -> gateway.getExchangeRate(CurrencyCode.USD, CurrencyCode.BRL))
                .isInstanceOf(CurrencyExchangeException.class)
                .hasMessage("Could not retrieve exchange rate.");
    }

    @Test
    void shouldThrowWhenClientReturnsNullRate() {
        when(client.getRate("USD", "BRL"))
                .thenReturn(new FrankfurterRateResponse(LocalDate.of(2026, 9, 15), "USD", "BRL", null));

        assertThatThrownBy(() -> gateway.getExchangeRate(CurrencyCode.USD, CurrencyCode.BRL))
                .isInstanceOf(CurrencyExchangeException.class)
                .hasMessage("Could not retrieve exchange rate.");
    }

    @Test
    void shouldTranslateFeignFailureToDomainException() {
        FeignException failure = mock(FeignException.class);
        when(client.getRate("USD", "BRL")).thenThrow(failure);

        assertThatThrownBy(() -> gateway.getExchangeRate(CurrencyCode.USD, CurrencyCode.BRL))
                .isInstanceOf(CurrencyExchangeException.class)
                .hasMessage("Currency exchange service is unavaiable.");
    }
}

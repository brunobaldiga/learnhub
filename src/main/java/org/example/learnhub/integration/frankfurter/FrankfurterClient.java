package org.example.learnhub.integration.frankfurter;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "frankfurterClient",
        url = "${currency.frankfurter.url}"
)
public interface FrankfurterClient {
    @GetMapping("/v2/rate/{from}/{to}")
    FrankfurterRateResponse getRate(
            @PathVariable("from") String from,
            @PathVariable("to") String to
    );
}

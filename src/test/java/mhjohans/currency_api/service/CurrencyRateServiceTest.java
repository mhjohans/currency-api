package mhjohans.currency_api.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;

@SpringBootTest
class CurrencyRateServiceTest {

    @Autowired
    private CurrencyRateService currencyRateService;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient currencyRateApiClient;

    @BeforeEach
    void setUp() {
        // Empty the caches
        currencyRateService.evictCurrencyRatesCache();
        currencyRateService.evictSupportedCurrenciesCache();
        // Mock the supported currencies to include USD and EUR
        List<CurrencyDTO> supportedCurrencies =
                List.of(new CurrencyDTO("USD", "12345", 2, "US Dollar", true),
                        new CurrencyDTO("EUR", "12345", 2, "Euro", true));
        when(currencyRateApiClient.get().uri("/currencies").retrieve()
                .body(new ParameterizedTypeReference<List<CurrencyDTO>>() {}))
                        .thenReturn(supportedCurrencies);
        // Mock the currency rate from USD to EUR
        CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO("USD", "EUR", 0.85, LocalDate.now());
        when(currencyRateApiClient.get().uri("/rates/{from}/{to}", "USD", "EUR").retrieve()
                .body(CurrencyRateDTO.class)).thenReturn(currencyRateDTO);
    }

    @Test
    void testGetSupportedCurrenciesCache() {
        // Call the method 3 times and verify that it only calls the API once
        for (int i = 0; i < 3; i++) {
            currencyRateService.getSupportedCurrencies();
        }
        verify(currencyRateApiClient.get().uri("/currencies").retrieve(), times(1))
                .body(new ParameterizedTypeReference<List<CurrencyDTO>>() {});
    }

    @Test
    void testGetCurrencyRateCache() {
        for (int i = 0; i < 3; i++) {
            currencyRateService.getCurrencyRate("USD", "EUR");
        }
        verify(currencyRateApiClient.get().uri("/rates/{from}/{to}", "USD", "EUR").retrieve(),
                times(1)).body(CurrencyRateDTO.class);
    }

    // TODO: Add resilience tests

    // TODO: Add fallback tests

}

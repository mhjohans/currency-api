package mhjohans.currency_api.services;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;
import mhjohans.currency_api.exceptions.ExternalApiException;

@SpringBootTest(properties = {"CURRENCY_API_KEY=d4e5f6a7-b8c9-4d0e-8f1a-2b3c4d5e6f7a"})
class CurrencyRateServiceTest {

    @Autowired
    private CurrencyRateService currencyRateService;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        // Empty the caches
        currencyRateService.evictCurrencyRatesCache();
        currencyRateService.evictSupportedCurrenciesCache();
        // Mock the supported currencies to include USD and EUR
        Set<CurrencyDTO> supportedCurrencies =
                Set.of(new CurrencyDTO("USD", "12345", 2, "US Dollar", true),
                        new CurrencyDTO("EUR", "12345", 2, "Euro", true));
        when(restClient.get().uri("/currencies").retrieve()
                .body(new ParameterizedTypeReference<Set<CurrencyDTO>>() {}))
                        .thenReturn(supportedCurrencies);
        // Mock the currency rate from USD to EUR
        CurrencyRateDTO currencyRate = new CurrencyRateDTO("USD", "EUR", 0.85, LocalDate.now());
        when(restClient.get().uri("/rates/{from}/{to}", "USD", "EUR").retrieve()
                .body(CurrencyRateDTO.class)).thenReturn(currencyRate);
    }

    @Test
    void testSupportedCurrenciesCache() throws ExternalApiException {
        // Call the method 3 times and verify that it only calls the API once
        for (int i = 0; i < 3; i++) {
            currencyRateService.getSupportedCurrencies();
        }
        verify(restClient.get().uri("/currencies").retrieve(), times(1))
                .body(new ParameterizedTypeReference<Set<CurrencyDTO>>() {});
    }

    @Test
    void testCurrencyRateCache() throws ExternalApiException {
        for (int i = 0; i < 3; i++) {
            currencyRateService.getCurrencyRate("USD", "EUR");
        }
        verify(restClient.get().uri("/rates/{from}/{to}", "USD", "EUR").retrieve(), times(1))
                .body(CurrencyRateDTO.class);
    }

}

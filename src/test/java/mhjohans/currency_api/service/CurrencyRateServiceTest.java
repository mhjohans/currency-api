package mhjohans.currency_api.service;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;

public class CurrencyRateServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient currencyRateApiClient;

    @BeforeEach
    public void setUp() {
        // Set the locale for formatting
        LocaleContextHolder.setLocale(Locale.US);
        // Initialize the mock objects
        MockitoAnnotations.openMocks(this);
        // Mock the supported currencies to include USD and EUR
        List<CurrencyDTO> supportedCurrencies = List.of(new CurrencyDTO("USD", "12345", 2, "US Dollar", true), new CurrencyDTO("EUR", "12345", 2, "Euro", true));
        when(currencyRateApiClient.get().uri("/currencies").retrieve().body(new ParameterizedTypeReference<List<CurrencyDTO>>() {}))
            .thenReturn(supportedCurrencies);
        // Mock the currency rate from USD to EUR
        CurrencyRateDTO currencyRateDTO = new CurrencyRateDTO("USD", "EUR", 0.85, LocalDate.now());
        when(currencyRateApiClient.get().uri("/rates/{from}/{to}", "USD", "EUR").retrieve().body(CurrencyRateDTO.class))
            .thenReturn(currencyRateDTO);
    }

}

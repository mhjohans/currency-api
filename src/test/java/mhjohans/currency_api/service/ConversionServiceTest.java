package mhjohans.currency_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;

public class ConversionServiceTest {

    @InjectMocks
    private ConversionService conversionService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient currencyRateApiClient;

    @BeforeEach
    public void setUp() {
        // Set the locale for formatting
        LocaleContextHolder.setLocale(Locale.US);
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

    @Test
    public void testConvertCurrency() {
        // Perform the conversion
        String result = conversionService.convertCurrency("USD", "EUR", 100);
        // Verify the format of the conversion result
        assertEquals("€85.00", result);
    }
    
    @Test
    public void testConvertCurrencyWithFinnishLocale() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("fi"));
        String result = conversionService.convertCurrency("USD", "EUR", 100);
        assertEquals("85,00 €", result);
    }

    @Test
    public void testConvertCurrencyWithZeroAmount() {
        String result = conversionService.convertCurrency("USD", "EUR", 0);
        assertEquals("€0.00", result);
    }

    @Test
    public void testConvertCurrencyWithNegativeAmount() {
        String result = conversionService.convertCurrency("USD", "EUR", -100);
        assertEquals("-€85.00", result);
    }
    @Test
    public void testConvertCurrencyWithNullCurrency() {
        // Expect an exception for null currency
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            conversionService.convertCurrency(null, "EUR", 100);
        });
        // Verify the exception message
        assertEquals("Currency code cannot be null", exception.getMessage());
    }

    @Test
    public void testConvertCurrencyWithUnsupportedCurrency() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertCurrency("USD", "GBP", 100);
        });
        assertEquals("Currency code not supported: GBP", exception.getMessage());
    }
    
}

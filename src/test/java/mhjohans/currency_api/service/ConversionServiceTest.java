package mhjohans.currency_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;

public class ConversionServiceTest {

    @Mock
    private CurrencyRateService currencyRateService;

    @InjectMocks
    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        // Set the locale for formatting
        LocaleContextHolder.setLocale(Locale.US);
        // Initialize the mock objects
        MockitoAnnotations.openMocks(this);
        // Mock the supported currencies to include USD and EUR
        List<String> supportedCurrencies = List.of("USD", "EUR");
        when(currencyRateService.getSupportedCurrencies()).thenReturn(supportedCurrencies);
        // Mock the currency rate from USD to EUR
        when(currencyRateService.getCurrencyRate("USD", "EUR")).thenReturn(0.85);
    }

    @Test
    void testConvertCurrency() {
        // Perform the conversion
        String result = conversionService.convertCurrency("USD", "EUR", 100);
        // Verify the format of the conversion result
        assertEquals("€85.00", result);
    }

    @Test
    void testConvertCurrencyWithFinnishLocale() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("fi"));
        String result = conversionService.convertCurrency("USD", "EUR", 100);
        assertEquals("85,00 €", result);
    }

    @Test
    void testConvertCurrencyWithZeroAmount() {
        String result = conversionService.convertCurrency("USD", "EUR", 0);
        assertEquals("€0.00", result);
    }

    @Test
    void testConvertCurrencyWithNegativeAmount() {
        String result = conversionService.convertCurrency("USD", "EUR", -100);
        assertEquals("-€85.00", result);
    }

    @Test
    void testConvertCurrencyWithNullCurrency() {
        // Expect an exception for null currency
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertCurrency(null, "EUR", 100);
        });
        // Verify the exception message
        assertEquals("Currency code cannot be null", exception.getMessage());
    }

    @Test
    void testConvertCurrencyWithEmptyCurrency() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertCurrency("", "EUR", 100);
        });
        assertEquals("Invalid currency code: string cannot be empty", exception.getMessage());
    }

    @Test
    void testConvertCurrencyWithUnsupportedCurrency() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            conversionService.convertCurrency("USD", "GBP", 100);
        });
        assertEquals("Currency code not supported: GBP", exception.getMessage());
    }

}

package mhjohans.currency_api.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import mhjohans.currency_api.exceptions.ExternalApiException;
import mhjohans.currency_api.exceptions.InvalidCurrencyException;

class ConversionServiceTest {

    @Mock
    private CurrencyRateService currencyRateService;

    @InjectMocks
    private ConversionService conversionService;

    @BeforeEach
    void setUp() throws ExternalApiException {
        // Set the locale for formatting
        LocaleContextHolder.setLocale(Locale.US);
        // Initialize the mock objects
        MockitoAnnotations.openMocks(this);
        // Mock the supported currencies to include USD and EUR
        Set<String> supportedCurrencies = Set.of("USD", "EUR");
        when(currencyRateService.getSupportedCurrencies()).thenReturn(supportedCurrencies);
        // Mock the currency rate from USD to EUR
        when(currencyRateService.getCurrencyRate("USD", "EUR")).thenReturn(0.85);
    }

    @Test
    void testConvertCurrency() throws InvalidCurrencyException, ExternalApiException {
        // Perform the conversion
        String result = conversionService.convert("USD", "EUR", 100);
        // Verify the format of the conversion result
        assertEquals("€85.00", result);
    }

    @Test
    void testConvertCurrencyWithFinnishLocale()
            throws InvalidCurrencyException, ExternalApiException {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("fi"));
        String result = conversionService.convert("USD", "EUR", 100);
        assertEquals("85,00 €", result);
    }

    @Test
    void testConvertCurrencyWithZeroAmount() throws InvalidCurrencyException, ExternalApiException {
        String result = conversionService.convert("USD", "EUR", 0);
        assertEquals("€0.00", result);
    }

    @Test
    void testConvertCurrencyWithNegativeAmount()
            throws InvalidCurrencyException, ExternalApiException {
        String result = conversionService.convert("USD", "EUR", -100);
        assertEquals("-€85.00", result);
    }

    @Test
    void testConvertCurrencyWithNullCurrency() {
        // Expect an exception for null currency
        InvalidCurrencyException exception = assertThrows(InvalidCurrencyException.class, () -> {
            conversionService.convert(null, "EUR", 100);
        });
        // Verify the exception message
        assertEquals("Currency code cannot be null", exception.getMessage());
    }

    @Test
    void testConvertCurrencyWithEmptyCurrency() {
        InvalidCurrencyException exception = assertThrows(InvalidCurrencyException.class, () -> {
            conversionService.convert("", "EUR", 100);
        });
        assertEquals("Invalid currency code: currency code cannot be empty",
                exception.getMessage());
    }

    @Test
    void testConvertCurrencyWithUnsupportedCurrency() {
        InvalidCurrencyException exception = assertThrows(InvalidCurrencyException.class, () -> {
            conversionService.convert("USD", "GBP", 100);
        });
        assertEquals("Currency code not supported: GBP", exception.getMessage());
    }

}

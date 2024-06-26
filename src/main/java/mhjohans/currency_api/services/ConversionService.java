package mhjohans.currency_api.services;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ConversionService {

    private final CurrencyRateService currencyRateService;

    ConversionService(CurrencyRateService currencyRateService) {
        this.currencyRateService = currencyRateService;
    }

    /**
     * Converts a given value from one currency to another.
     *
     * @param sourceCurrencyCode the currency code to convert from as a string
     * @param targetCurrencyCode the currency code to convert to as a string
     * @param value the amount to convert as a double
     * @return the converted value as a string
     */
    public String convertCurrency(String sourceCurrencyCode, String targetCurrencyCode,
            double value) {
        validateCurrencyCode(sourceCurrencyCode);
        validateCurrencyCode(targetCurrencyCode);
        double convertedValue =
                currencyRateService.getCurrencyRate(sourceCurrencyCode, targetCurrencyCode) * value;
        return formatCurrencyValue(convertedValue, targetCurrencyCode);
    }

    private void validateCurrencyCode(String currencyCode) {
        // Check if the currency code is null
        if (currencyCode == null) {
            throw new IllegalArgumentException("Currency code cannot be null");
        }
        // Clean up the received currency code of whitespace and convert to uppercase
        String cleanedCurrencyCode = currencyCode.trim().toUpperCase();
        // Check if the currency code has correct length
        if (cleanedCurrencyCode.length() != 3) {
            throw new IllegalArgumentException("Invalid currency code: "
                    + (currencyCode.isEmpty() ? "currency code cannot be empty" : currencyCode));
        }
        // Check if the currency code is not on the list of supported currencies
        List<String> supportedCurrencies = currencyRateService.getSupportedCurrencies();
        if (!supportedCurrencies.contains(cleanedCurrencyCode)) {
            throw new IllegalArgumentException("Currency code not supported: " + currencyCode);
        }
    }

    private String formatCurrencyValue(double value, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        // Formats the amount as a localized currency string based on the 'Accept-Language' header in the 
        // request or the default runtime locale if header is not present
        NumberFormat numberFormat =
                NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale());
        numberFormat.setCurrency(currency);
        return numberFormat.format(value);
    }

}

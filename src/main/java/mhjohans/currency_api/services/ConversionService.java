package mhjohans.currency_api.services;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ConversionService {

    private static final Logger logger = LoggerFactory.getLogger(ConversionService.class);

    private final CurrencyRateService currencyRateService;

    ConversionService(CurrencyRateService currencyRateService) {
        this.currencyRateService = currencyRateService;
    }

    /**
     * Converts a given value from one currency to another.
     *
     * @param sourceCurrency the currency code to convert from as a string
     * @param targetCurrency the currency code to convert to as a string
     * @param value the amount to convert as a double
     * @return the converted value as a string
     */
    public String convert(String sourceCurrency, String targetCurrency, double value) {
        logger.debug("Converting {} {} to {}...", value, sourceCurrency, targetCurrency);
        validate(sourceCurrency);
        validate(targetCurrency);
        double convertedValue =
                currencyRateService.getCurrencyRate(sourceCurrency, targetCurrency) * value;
        logger.debug("Converted {} {} to {}.", value, sourceCurrency, convertedValue);
        return localize(convertedValue, targetCurrency);
    }

    private void validate(String currency) {
        logger.debug("Validating currency code: {}", currency);
        // Check if the currency code is null
        if (currency == null) {
            logger.debug("Currency code is null");
            throw new IllegalArgumentException("Currency code cannot be null");
        }
        // Clean up the received currency code of whitespace and convert to uppercase
        String cleanedCurrency = currency.trim().toUpperCase();
        logger.debug("Cleaned up currency code: {}", cleanedCurrency);
        // Check if the currency code has correct length
        if (cleanedCurrency.length() != 3) {
            logger.debug("Currency code has incorrect length: {}", cleanedCurrency);
            throw new IllegalArgumentException("Invalid currency code: "
                    + (currency.isEmpty() ? "currency code cannot be empty" : currency));
        }
        // Check if the currency code is not on the list of supported currencies
        List<String> supportedCurrencies = currencyRateService.getSupportedCurrencies();
        if (!supportedCurrencies.contains(cleanedCurrency)) {
            logger.debug("Currency code not supported: {}", cleanedCurrency);
            throw new IllegalArgumentException("Currency code not supported: " + currency);
        }
        logger.debug("Currency code validated successfully");
    }

    private static String localize(double value, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        // Formats the amount as a localized currency string based on the 'Accept-Language' header in the 
        // request or the default runtime locale if header is not present
        NumberFormat numberFormat =
                NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale());
        numberFormat.setCurrency(currency);
        return numberFormat.format(value);
    }

}

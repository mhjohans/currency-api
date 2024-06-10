package mhjohans.currency_api.service;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ConversionService {

    @Autowired
    private CurrencyRateService currencyRateService;

    public String convertCurrency(String fromCurrencyCode, String toCurrencyCode, double amount) {
        validateCurrency(fromCurrencyCode);
        validateCurrency(toCurrencyCode);
        double convertedAmount = currencyRateService.getCurrencyRate(fromCurrencyCode, toCurrencyCode) * amount;
        return formatCurrencyAmount(convertedAmount, toCurrencyCode);
    }
    
    private void validateCurrency(String currencyCode) {
        Objects.requireNonNull(currencyCode, "Currency code cannot be null");
        List<String> supportedCurrencies = currencyRateService.getSupportedCurrencies();
        if (!supportedCurrencies.contains(currencyCode)) {
            throw new IllegalArgumentException("Currency code not supported: " + currencyCode);
        }
    }

    private String formatCurrencyAmount(double amount, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        // Formats the amount as a localized currency string based on the 'Accept-Language' header in the request or the default runtime locale if header is not present
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale());
        numberFormat.setCurrency(currency);
        return numberFormat.format(amount);
    }
    
}

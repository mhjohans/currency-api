package mhjohans.currency_api.service;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;

@Service
public class CurrencyApiService {

    @Autowired
    private RestClient currencyRateApiClient;

    public String convertCurrency(String fromCurrencyCode, String toCurrencyCode, double amount) {
        validateCurrency(fromCurrencyCode);
        validateCurrency(toCurrencyCode);
        double convertedAmount = getCurrencyRate(fromCurrencyCode, toCurrencyCode) * amount;
        return formatCurrencyAmount(convertedAmount, toCurrencyCode);
    }

    private List<String> getSupportedCurrencies() {
        return currencyRateApiClient.get().uri("/currencies").retrieve().body(new ParameterizedTypeReference<List<CurrencyDTO>>() {}).stream().map(CurrencyDTO::code).toList();
    }
    
    private double getCurrencyRate(String fromCurrencyCode, String toCurrencyCode) {
        return currencyRateApiClient.get().uri("/rates/{from}/{to}", fromCurrencyCode, toCurrencyCode).retrieve().body(CurrencyRateDTO.class).quote();
    }
    
    private String formatCurrencyAmount(double amount, String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        // Formats the amount as a localized currency string based on the 'Accept-Language' header in the request or the default runtime locale if header is not present
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale());
        numberFormat.setCurrency(currency);
        return numberFormat.format(amount);
    }

    private void validateCurrency(String currencyCode) {
        List<String> supportedCurrencies = getSupportedCurrencies();
        if (!supportedCurrencies.contains(currencyCode)) {
            throw new IllegalArgumentException("Currency not supported: " + currencyCode);
        }
    }

}

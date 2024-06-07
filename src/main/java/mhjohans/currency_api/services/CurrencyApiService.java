package mhjohans.currency_api.services;

import java.text.NumberFormat;
import java.util.Currency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrencyApiService {

    @Autowired
    private CurrencyRateService currencyRateService;

    public String convertCurrency(String fromCurrencyCode, String toCurrencyCode, double amount) {
        // TODO: Add validation based on the currencies supported on SWOP
        double convertedAmount = currencyRateService.getCurrencyRate(fromCurrencyCode, toCurrencyCode) * amount;
        return getLocalizedText(toCurrencyCode, convertedAmount);
    }

    private String getLocalizedText(String currencyCode, double amount) {
        Currency currency = Currency.getInstance(currencyCode);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale());
        numberFormat.setCurrency(currency);
        return numberFormat.format(amount);
    }

}

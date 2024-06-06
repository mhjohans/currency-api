package mhjohans.currency_api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyApiService {

    @Autowired
    private CurrencyRateService currencyRateService;

    public double convertCurrency(final String from, final String to, final double amount) {
        return currencyRateService.getCurrencyRate(from, to) * amount;
    }

}

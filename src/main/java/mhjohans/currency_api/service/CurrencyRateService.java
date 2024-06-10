package mhjohans.currency_api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;

@Service
public class CurrencyRateService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

    @Autowired
    private RestClient currencyRateApiClient;

    @Cacheable(value = "supportedCurrencies")
    public List<String> getSupportedCurrencies() {
        logger.trace("Getting supported currencies from API");
        return currencyRateApiClient.get().uri("/currencies").retrieve().body(new ParameterizedTypeReference<List<CurrencyDTO>>() {}).stream().map(CurrencyDTO::code).toList();
    }

    @Cacheable(value = "currencyRates")
    public double getCurrencyRate(String fromCurrencyCode, String toCurrencyCode) {
        logger.trace("Getting currency rate from API for {} to {}", fromCurrencyCode, toCurrencyCode);
        return currencyRateApiClient.get().uri("/rates/{from}/{to}", fromCurrencyCode, toCurrencyCode).retrieve().body(CurrencyRateDTO.class).quote();
    }
 
    /**
     * Empties the cache for supported currencies on a scheduled interval defined in the application property "currency_rates_api.supported_currencies_ttl".
     */
    @Scheduled(fixedRateString = "${currency_rates_api.supported_currencies_ttl}")
    void evictSupportedCurrenciesCache() {
        logger.debug("Evicting supported currencies cache");
    }

    /**
     * Empties the cache for currency rates on a scheduled interval defined in the application property "currency_rates_api.currency_rates_ttl".
     */
    @Scheduled(fixedRateString = "${currency_rates_api.currency_rates_ttl}")
    void evictCurrencyRatesCache() {
        logger.debug("Evicting currency rates cache");
    }

}

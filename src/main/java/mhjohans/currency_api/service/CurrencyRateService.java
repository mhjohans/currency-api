package mhjohans.currency_api.service;

import java.util.List;
import java.util.Objects;

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

    @Cacheable("supportedCurrencies")
    public List<String> getSupportedCurrencies() {
        // TODO: Add resilience
        logger.debug("Getting supported currencies from API");
        List<CurrencyDTO> supportedCurrencies = currencyRateApiClient.get().uri("/currencies").retrieve().body(new ParameterizedTypeReference<List<CurrencyDTO>>() {});
        logger.trace("Got supported currencies from API: {}", supportedCurrencies);
        Objects.requireNonNull(supportedCurrencies, "Supported currencies cannot be null");
        return supportedCurrencies.stream().map(CurrencyDTO::code).toList();
    }

    @Cacheable("currencyRates")
    public double getCurrencyRate(String fromCurrencyCode, String toCurrencyCode) {
        logger.debug("Getting currency rate from API for {} to {}", fromCurrencyCode, toCurrencyCode);
        CurrencyRateDTO currencyRate = currencyRateApiClient.get().uri("/rates/{from}/{to}", fromCurrencyCode, toCurrencyCode).retrieve().body(CurrencyRateDTO.class);
        logger.trace("Got currency rate from API for {} to {} with value {}: {}", fromCurrencyCode, toCurrencyCode, currencyRate, currencyRate);
        Objects.requireNonNull(currencyRate, "Currency rate cannot be null");
        return currencyRate.quote();
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

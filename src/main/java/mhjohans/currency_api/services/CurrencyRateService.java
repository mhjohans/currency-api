package mhjohans.currency_api.services;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import mhjohans.currency_api.dtos.CurrencyDTO;
import mhjohans.currency_api.dtos.CurrencyRateDTO;

@Service
@CircuitBreaker(name = "currencyRateServiceCircuitBreaker")
public class CurrencyRateService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyRateService.class);

    private final RestClient currencyRateApiClient;

    @Value("${currency-rates-api.supported-currencies.fallback}")
    private List<String> fallbackSupportedCurrencies;

    CurrencyRateService(RestClient currencyRateApiClient) {
        this.currencyRateApiClient = currencyRateApiClient;
    }

    @Cacheable("supportedCurrencies")
    // TODO: Having fallback values means that the values won't be updated until the
    // cache expires even if the API starts working.
    @Retry(name = "supportedCurrenciesRetry", fallbackMethod = "getSupportedCurrenciesFallback")
    public List<String> getSupportedCurrencies() {
        logger.debug("Getting supported currencies from API");
        List<CurrencyDTO> supportedCurrencies = currencyRateApiClient.get().uri("/currencies")
                .retrieve().body(new ParameterizedTypeReference<List<CurrencyDTO>>() {});
        logger.trace("Got supported currencies from API: {}", supportedCurrencies);
        Objects.requireNonNull(supportedCurrencies, "Supported currencies cannot be null");
        return supportedCurrencies.stream().map(CurrencyDTO::code).toList();
    }

    @Cacheable(value = "currencyRates", sync = true)
    @Retry(name = "currencyRateRetry")
    public double getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode) {
        logger.debug("Getting currency rate from API: source currency {}, target currency {}",
                sourceCurrencyCode, targetCurrencyCode);
        CurrencyRateDTO currencyRate = currencyRateApiClient.get()
                .uri("/rates/{from}/{to}", sourceCurrencyCode, targetCurrencyCode).retrieve()
                .body(CurrencyRateDTO.class);
        logger.trace("Got currency rate from API: {}", currencyRate);
        Objects.requireNonNull(currencyRate, "Currency rate cannot be null");
        return currencyRate.quote();
    }

    /**
     * Empties the cache for supported currencies on a scheduled interval defined in
     * the application properties file.
     */
    @Scheduled(fixedRateString = "${currency-rates-api.supported-currencies.cache-ttl}")
    @CacheEvict(value = "supportedCurrencies", allEntries = true)
    void evictSupportedCurrenciesCache() {
        logger.trace("Evicting supported currencies cache");
    }

    /**
     * Empties the cache for currency rates on a scheduled interval defined in the
     * application properties file.
     */
    @Scheduled(fixedRateString = "${currency-rates-api.currency-rates.cache-ttl}")
    @CacheEvict(value = "currencyRates", allEntries = true)
    void evictCurrencyRatesCache() {
        logger.trace("Evicting currency rates cache");
    }

    @SuppressWarnings("unused")
    private List<String> getSupportedCurrenciesFallback(Exception e) {
        logger.warn("Could not retrieve supported currencies, using fallback values: {}",
                fallbackSupportedCurrencies, e);
        return fallbackSupportedCurrencies;
    }

}

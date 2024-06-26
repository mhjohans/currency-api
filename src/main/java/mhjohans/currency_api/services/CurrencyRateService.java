package mhjohans.currency_api.services;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    CurrencyRateService(RestClient currencyRateApiClient) {
        this.currencyRateApiClient = currencyRateApiClient;
    }

    /**
     * Retrieves the list of supported currencies from the external API.
     * <ul>
     * <li>Result is cached to improve performance on repeat calls.
     * <li>A retry policy is used to retry the API call if it fails.
     * <li>A circuit breaker is used to stop the API call if it fails too many times.
     * </ul>
     * 
     * See {@link mhjohans.currency_api.configurations.ResilienceConfiguration} for the resilience configuration.
     *
     * @return  a list of currency code strings representing the supported currencies
     */
    @Cacheable("supportedCurrencies")
    @Retry(name = "supportedCurrenciesRetry")
    public List<String> getSupportedCurrencies() {
        logger.debug("Getting supported currencies from external API");
        List<CurrencyDTO> supportedCurrencies = currencyRateApiClient.get().uri("/currencies")
                .retrieve().body(new ParameterizedTypeReference<List<CurrencyDTO>>() {});
        Objects.requireNonNull(supportedCurrencies, "Supported currencies cannot be null");
        if (logger.isTraceEnabled()) {
            logger.trace("Got supported currencies from external API: {}", supportedCurrencies);
        } else {
            logger.debug("Got {} supported currencies from external API",
                    supportedCurrencies.size());
        }
        return supportedCurrencies.stream().map(CurrencyDTO::code).toList();
    }

    /**
     * Retrieves the currency rate from the external API based on the source and target currency codes.
     * <ul>
     * <li>Result is cached to improve performance on repeat calls.
     * <li>A retry policy is used to retry the API call if it fails.
     * <li>A circuit breaker is used to stop the API call if it fails too many times.
     * </ul>
     * 
     * See {@link mhjohans.currency_api.configurations.ResilienceConfiguration} for the resilience configuration.
     *
     * @param sourceCurrencyCode the code of the source currency
     * @param targetCurrencyCode the code of the target currency
     * @return the currency rate as a double
     */
    @Cacheable(value = "currencyRates", sync = true)
    @Retry(name = "currencyRateRetry")
    public double getCurrencyRate(String sourceCurrencyCode, String targetCurrencyCode) {
        logger.debug(
                "Getting currency rate from external API: source currency {}, target currency {}",
                sourceCurrencyCode, targetCurrencyCode);
        CurrencyRateDTO currencyRate = currencyRateApiClient.get()
                .uri("/rates/{from}/{to}", sourceCurrencyCode, targetCurrencyCode).retrieve()
                .body(CurrencyRateDTO.class);
        logger.debug("Got currency rate from external API: {}", currencyRate);
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

}

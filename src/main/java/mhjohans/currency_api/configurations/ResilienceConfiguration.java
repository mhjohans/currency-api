package mhjohans.currency_api.configurations;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;

@Configuration
public class ResilienceConfiguration {

    /**
     * Configures the circuit breaker for all calls in the currency rate service.
     */
    @Bean
    CircuitBreakerConfigCustomizer circuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of("currencyRateServiceCircuitBreaker",
                builder -> builder.failureRateThreshold(60).slidingWindowSize(5)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(2));
    }

    /**
     * Configures the retry policy for retrieving the list of supported currencies in the currency rate service.
     */
    @Bean
    RetryConfigCustomizer supportedCurrenciesRetryConfigCustomizer() {
        return RetryConfigCustomizer.of("supportedCurrenciesRetry",
                builder -> builder.maxAttempts(5).waitDuration(Duration.ofMillis(500)));
    }

    /**
     * Configures the retry policy for retrieving the currency rate in the currency rate service.
     */
    @Bean
    RetryConfigCustomizer currencyRateRetryConfigCustomizer() {
        return RetryConfigCustomizer.of("currencyRateRetry",
                builder -> builder.maxAttempts(3).waitDuration(Duration.ofMillis(300)));
    }

}

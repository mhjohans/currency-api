package mhjohans.currency_api.configuration;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;

@Configuration
public class ResilienceConfiguration {

    @Bean
    CircuitBreakerConfigCustomizer circuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer.of("currencyRateServiceCircuitBreaker",
                builder -> builder.failureRateThreshold(60).slidingWindowSize(5)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(2));
    }

    @Bean
    RetryConfigCustomizer supportedCurrenciesRetryConfigCustomizer() {
        return RetryConfigCustomizer.of("supportedCurrenciesRetry",
                builder -> builder.maxAttempts(5).waitDuration(Duration.ofMillis(500)));
    }

    @Bean
    RetryConfigCustomizer currencyRateRetryConfigCustomizer() {
        return RetryConfigCustomizer.of("currencyRateRetry",
                builder -> builder.maxAttempts(3).waitDuration(Duration.ofMillis(300)));
    }

}

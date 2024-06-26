package mhjohans.currency_api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;

@Configuration
public class MetricsConfiguration {

    private final MeterRegistry meterRegistry;

    @Value("${observability.metrics.controller-convert-timer.name}")
    private String timerName;

    @Value("${observability.metrics.controller-convert-counter.name}")
    private String counterName;

    MetricsConfiguration(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    private void init() {
        Timer.builder(timerName).description("Time taken with a call to convert endpoint")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99).tag("endpoint", "convert")
                .register(meterRegistry);
        Counter.builder(counterName).description("Number of calls to convert endpoint")
                .tag("endpoint", "convert").register(meterRegistry);
    }

    @Bean
    CountedAspect countedAspect() {
        return new CountedAspect(meterRegistry);
    }

    @Bean
    TimedAspect timedAspect() {
        return new TimedAspect(meterRegistry);
    }


}

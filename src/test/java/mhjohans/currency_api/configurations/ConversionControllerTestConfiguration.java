package mhjohans.currency_api.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
public class ConversionControllerTestConfiguration {

    @Bean
    MeterRegistry testMeterRegistry() {
        return new SimpleMeterRegistry();
    }
}

package mhjohans.currency_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import mhjohans.currency_api.services.ConversionService;

@RestController
@RequestMapping("/${spring.application.name}")
public class ConversionController {

    private static final String CONVERT_TIMER_NAME = "controller.convert.timer";

    private static final String CONVERT_FAIL_COUNTER_NAME = "controller.convert.fail.counter";

    private final ConversionService conversionService;

    ConversionController(ConversionService conversionService, MeterRegistry meterRegistry) {
        this.conversionService = conversionService;
        initMetrics(meterRegistry);
    }

    private void initMetrics(MeterRegistry meterRegistry) {
        Timer.builder(CONVERT_TIMER_NAME).description("Time taken with a call to convert endpoint")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99).tag("endpoint", "convert")
                .register(meterRegistry);
        Counter.builder(CONVERT_FAIL_COUNTER_NAME)
                .description("Number of failed calls to convert endpoint")
                .tag("endpoint", "convert").register(meterRegistry);
    }

    /**
     * HTTP GET endpoint that converts the given decimal amount from one currency to
     * another. Client-side HTTP caching is disabled, instead always return the latest data and use
     * the internal cache if available.
     *
     * @param source the currency code to convert from as a string
     * @param target the currency code to convert to as a string
     * @param value  the amount to convert as a double
     * @return the converted amount as a string
     */
    @GetMapping("/convert")
    @Timed(CONVERT_TIMER_NAME)
    @Counted(value = CONVERT_FAIL_COUNTER_NAME, recordFailuresOnly = true)
    public String convertCurrency(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) {
        // TODO: Add logging
        try {
            return conversionService.convertCurrency(source, target, value);
        } catch (IllegalArgumentException e) {
            // Received invalid request parameters
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RestClientException e) {
            // Could not get a valid response from the external currency rate API
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}

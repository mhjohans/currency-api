package mhjohans.currency_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import mhjohans.currency_api.services.ConversionService;

@RestController
@RequestMapping("/${spring.application.name}")
public class ConversionController {

    private static final Logger logger = LoggerFactory.getLogger(ConversionController.class);

    private final ConversionService conversionService;

    private final MeterRegistry meterRegistry;

    private Timer convertTimer;

    private Counter convertFailCounter;

    ConversionController(ConversionService conversionService, MeterRegistry meterRegistry) {
        this.conversionService = conversionService;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    private void initMetrics() {
        convertTimer = Timer.builder("controller.convert.timer")
                .description("Time taken with a call to convert endpoint")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99).tag("endpoint", "convert")
                .register(meterRegistry);
        convertFailCounter = Counter.builder("controller.convert.fail.counter")
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
    public String convert(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) {
        try {
            logger.debug("Received request for conversion from {} to {} with value {}", source,
                    target, value);
            String result = convertTimer
                    .recordCallable(() -> conversionService.convert(source, target, value));
            logger.debug("Finished response for conversion request, result: {}", result);
            return result;
        } catch (Exception e) {
            convertFailCounter.increment();
            if (e instanceof IllegalArgumentException) {
                // Received invalid request parameters
                logger.debug("Received invalid request parameters: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
            } else if (e instanceof RestClientException) {
                // Could not get a valid response from the external currency rate API
                logger.warn(
                        "Could not get a valid response from the external currency rate API: {}",
                        e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                        e);
            }
            // Unexpected error
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}

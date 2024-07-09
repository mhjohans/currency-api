package mhjohans.currency_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
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
     * HTTP GET endpoint that converts the given double value from one currency to another 
     * with the current exchange rate retrieved from an external API. Client-side HTTP caching is disabled, 
     * instead always return the latest data and use the internal cache if available.
     *
     * @param source the currency to convert from as a three-letter currency code string
     * @param target the currency to convert to as a three-letter currency code string
     * @param value  the value to convert as a double
     * @return the converted value as a localized currency string
     */
    @GetMapping("/convert")
    public ResponseEntity<String> convert(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) {
        try {
            logger.debug("Received request for conversion from {} to {} with value {}", source,
                    target, value);
            String result = convertTimer
                    .recordCallable(() -> conversionService.convert(source, target, value));
            logger.debug("Finished response for conversion request, result: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            convertFailCounter.increment();
            HttpStatus status;
            String message = e.getMessage();
            if (e instanceof IllegalArgumentException) {
                // Received invalid request parameters
                logger.debug("Received invalid request parameters: {}", message);
                status = HttpStatus.BAD_REQUEST;
            } else if (e instanceof RestClientException) {
                // Could not get a valid response from the external currency rate API
                logger.warn("Could not get a valid response from external currency rate API: {}",
                        message);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                message = String.format(
                        "Could not get a valid response from external currency rate API:%nResponse [%s]",
                        message);
            } else {
                // Unexpected error
                logger.error("Unexpected error occurred: {}", message);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return ResponseEntity.status(status).body("Error: " + status.value() + " - " + message);
        }
    }

}

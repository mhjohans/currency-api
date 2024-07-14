package mhjohans.currency_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import mhjohans.currency_api.exceptions.ExternalApiException;
import mhjohans.currency_api.exceptions.InvalidCurrencyException;
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
    public String convert(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) throws Exception {
        logger.debug("Received request for conversion from {} to {} with value {}", source, target,
                value);
        String result =
                convertTimer.recordCallable(() -> conversionService.convert(source, target, value));
        logger.debug("Finished response for conversion request, result: {}", result);
        return result;
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    ResponseEntity<String> handleInvalidCurrencyException(InvalidCurrencyException e) {
        logger.warn("Received invalid currency code request parameter: {}", e.getMessage());
        convertFailCounter.increment();
        return getResponse(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<String> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        logger.warn("Received invalid request parameter: {}", e.getMessage());
        convertFailCounter.increment();
        return getResponse(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(ExternalApiException.class)
    ResponseEntity<String> handleExternalApiException(ExternalApiException e) {
        logger.warn("Could not get a valid response from external currency rate API: {}",
                e.getMessage());
        convertFailCounter.increment();
        return getResponse(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    @ExceptionHandler(Throwable.class)
    ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred: {}", e.getMessage());
        convertFailCounter.increment();
        return getResponse(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    private static ResponseEntity<String> getResponse(HttpStatus status, Exception e) {
        return ResponseEntity.status(status)
                .body("Error: " + status.value() + " - " + e.getMessage());
    }

}

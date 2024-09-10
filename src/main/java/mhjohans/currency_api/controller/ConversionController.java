package mhjohans.currency_api.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import mhjohans.currency_api.dtos.response.ErrorResponseDTO;
import mhjohans.currency_api.dtos.response.FailResponseDTO;
import mhjohans.currency_api.dtos.response.SuccessResponseDTO;
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
    @GetMapping(value = "/convert", produces = MediaType.APPLICATION_JSON_VALUE)
    SuccessResponseDTO<String> convert(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) throws Exception {
        logger.debug("Received request for conversion from {} to {} with value {}", source, target,
                value);
        String result =
                convertTimer.recordCallable(() -> conversionService.convert(source, target, value));
        logger.debug("Finished response for conversion request, result: {}", result);
        return new SuccessResponseDTO<>(result);
    }

    @ExceptionHandler(InvalidCurrencyException.class)
    ResponseEntity<FailResponseDTO> handleInvalidCurrencyException(InvalidCurrencyException e) {
        logger.warn("Received invalid currency code request parameter: {}", e.getMessage());
        convertFailCounter.increment();
        return getFailResponse(e);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<FailResponseDTO> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        logger.warn("Received invalid request parameter: {}", e.getMessage());
        convertFailCounter.increment();
        return getFailResponse(e);
    }

    @ExceptionHandler(ExternalApiException.class)
    ResponseEntity<ErrorResponseDTO> handleExternalApiException(ExternalApiException e) {
        logger.warn("Could not get a valid response from external currency rate API: {}",
                e.getMessage());
        convertFailCounter.increment();
        return getErrorResponse(e);
    }

    @ExceptionHandler(Throwable.class)
    ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred: {}", e.getMessage());
        convertFailCounter.increment();
        return getErrorResponse(e);
    }

    /*
     * Return JSend formatted error response with status code 400 indicating an invalid request
     */
    private static ResponseEntity<FailResponseDTO> getFailResponse(Exception e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new FailResponseDTO(e.getMessage()));
    }

    /*
     * Return JSend formatted error response with status code 500 indicating an internal server error
     */
    private static ResponseEntity<ErrorResponseDTO> getErrorResponse(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> data = null;
        if (e instanceof ExternalApiException externalApiException) {
            // Parse the response body from the exception as JSON
            String responseBody =
                    externalApiException.getClientResponseException().getResponseBodyAsString();
            try {
                data = new ObjectMapper().readValue(responseBody,
                        new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException jsonProcessingException) {
                logger.error("Failed to parse response body as JSON: {}",
                        jsonProcessingException.getMessage());
            }
        }
        return ResponseEntity.status(status)
                .body(new ErrorResponseDTO(e.getMessage(), status.value(), data));
    }

}

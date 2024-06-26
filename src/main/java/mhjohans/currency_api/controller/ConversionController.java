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
import mhjohans.currency_api.service.ConversionService;

@RestController
@RequestMapping("/${spring.application.name}")
public class ConversionController {

    private final ConversionService currencyApiService;

    ConversionController(ConversionService currencyApiService) {
        this.currencyApiService = currencyApiService;
    }

    /**
     * HTTP GET endpoint that converts the given decimal amount from one currency to
     * another.
     *
     * @param source the currency code to convert from as a string
     * @param target the currency code to convert to as a string
     * @param value  the amount to convert as a double
     * @return the converted amount as a string
     */
    @GetMapping("/convert")
    @Timed("${observability.metrics.controller-convert-timer.name}")
    @Counted("${observability.metrics.controller-convert-counter.name}")
    public String convertCurrency(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) {
        // TODO: Add logging
        // TODO: Add code injection protection
        // No HTTP caching is required, instead always return the latest data and use
        // the internal cache if available
        try {
            return currencyApiService.convertCurrency(source, target, value);
        } catch (IllegalArgumentException e) {
            // Received invalid request parameters
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RestClientException e) {
            // Could not get a valid response from the external currency rate API
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

}

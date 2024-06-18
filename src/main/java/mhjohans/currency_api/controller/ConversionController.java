package mhjohans.currency_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import mhjohans.currency_api.service.ConversionService;

@RestController
@RequestMapping("/${spring.application.name}")
// TODO: Add logging
public class ConversionController {

    @Autowired
    private ConversionService currencyApiService;

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
    public String convertCurrency(@RequestParam String source, @RequestParam String target,
            @RequestParam double value) {
        // TODO: Add authentication
        // TODO: Add CSRF and CSP for security
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

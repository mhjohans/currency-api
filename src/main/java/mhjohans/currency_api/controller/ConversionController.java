package mhjohans.currency_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mhjohans.currency_api.service.ConversionService;

@RestController
@RequestMapping("/${spring.application.name}")
public class ConversionController {

    @Autowired
    private ConversionService currencyApiService;

    /**
     * Converts the given amount from one currency to another.
     *
     * @param  from    the currency code to convert from as a string
     * @param  to      the currency code to convert to as a string
     * @param  amount  the amount to convert as a double
     * @return         the converted amount as a string
     */
    @GetMapping("/convert")
    public String convertCurrency(@RequestParam String from, @RequestParam String to, @RequestParam double amount) {
        // TODO: Add authentication
        // TODO: Add CSRF and CSP for security
        // TODO: Add mapping of errors to HTTP status codes
        return currencyApiService.convertCurrency(from, to, amount);
    }

}

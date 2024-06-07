package mhjohans.currency_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mhjohans.currency_api.service.CurrencyApiService;

@RestController
public class CurrencyApiController {

    @Autowired
    private CurrencyApiService currencyApiService;

    /**
     * Converts the given amount from one currency to another.
     *
     * @param  from    the currency to convert from
     * @param  to      the currency to convert to
     * @param  amount  the amount to convert
     * @return         the converted amount as a string
     */
    @GetMapping("/convert")
    public String convertCurrency(@RequestParam String from, @RequestParam String to, @RequestParam double amount) {
        return currencyApiService.convertCurrency(from, to, amount);
    }

}

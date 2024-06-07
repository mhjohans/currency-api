package mhjohans.currency_api.dtos;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrencyRateDTO(@JsonProperty("base_currency") String baseCurrency, @JsonProperty("quote_currency") String quoteCurrency, double quote, LocalDate date) {
}

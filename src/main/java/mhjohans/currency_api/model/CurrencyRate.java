package mhjohans.currency_api.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrencyRate(@JsonProperty("base_currency") String baseCurrency, @JsonProperty("quote_currency") String quoteCurrency, @JsonProperty("quote") double quote, @JsonProperty("date") LocalDate date) {
}

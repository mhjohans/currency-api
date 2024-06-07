package mhjohans.currency_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrencyDTO(String code, @JsonProperty("numeric_code") String numericCode, @JsonProperty("decimal_digits") int decimalDigits, String name, boolean active) {
}


package mhjohans.currency_api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClientException;

import mhjohans.currency_api.configuration.TestWebSecurityConfiguration;
import mhjohans.currency_api.service.ConversionService;
import mhjohans.currency_api.service.CurrencyRateService;

@WebMvcTest(ConversionController.class)
@Import({ ConversionService.class, TestWebSecurityConfiguration.class })
public class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyRateService currencyRateService;

    @BeforeEach
    void setUp() {
        // Mock the supported currencies to include USD and EUR
        List<String> supportedCurrencies = List.of("USD", "EUR");
        when(currencyRateService.getSupportedCurrencies()).thenReturn(supportedCurrencies);
        // Mock the currency rate from USD to EUR
        when(currencyRateService.getCurrencyRate("USD", "EUR")).thenReturn(0.85);
    }

    @Test
    void testConvert() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/currency-api/convert?from=USD&to=EUR&amount=100"))
                .andExpect(status().isOk())
                .andExpect(content().string("€85.00"));
    }

    @Test
    void testConvertWithEmptyCurrency() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/currency-api/convert?from=&to=EUR&amount=100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertWithEmptyAmount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/currency-api/convert?from=USD&to=EUR&amount="))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertWithUnsupportedCurrency() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/currency-api/convert?from=USD&to=GBP&amount=100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConvertWithUnavailableExternalApi() throws Exception {
        when(currencyRateService.getCurrencyRate("USD", "EUR")).thenThrow(new RestClientException("N/A"));
        mockMvc.perform(MockMvcRequestBuilders.get("/currency-api/convert?from=USD&to=EUR&amount=100"))
                .andExpect(status().isInternalServerError());
    }

}

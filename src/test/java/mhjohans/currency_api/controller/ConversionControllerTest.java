package mhjohans.currency_api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClientException;
import mhjohans.currency_api.configurations.ConversionControllerTestConfiguration;
import mhjohans.currency_api.exceptions.ExternalApiException;
import mhjohans.currency_api.services.ConversionService;
import mhjohans.currency_api.services.CurrencyRateService;

@WebMvcTest(ConversionController.class)
@Import({ConversionService.class, ConversionControllerTestConfiguration.class})
@WithMockUser
class ConversionControllerTest {

        private static final String REQUEST_URL = "/currency-api/convert";

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CurrencyRateService currencyRateService;

        @BeforeEach
        void setUp() throws ExternalApiException {
                // Mock the supported currencies to include USD and EUR
                Set<String> supportedCurrencies = Set.of("USD", "EUR");
                when(currencyRateService.getSupportedCurrencies()).thenReturn(supportedCurrencies);
                // Mock the currency rate from USD to EUR
                when(currencyRateService.getCurrencyRate("USD", "EUR")).thenReturn(0.85);
        }

        @Test
        void testConvert() throws Exception {
                String jsonContent = "{\"status\":\"success\",\"data\":{\"result\":\"€85.00\"}}";
                performRequest("USD", "EUR", 100).andExpect(status().isOk())
                                .andExpect(content().json(jsonContent));
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCurrencies")
        void testConvertWithInvalidCurrencies(String source, String target, double value)
                        throws Exception {
                performRequest(source, target, value)
                                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
        }

        private static Stream<Arguments> provideInvalidCurrencies() {
                return Stream.of(Arguments.arguments("", "EUR", 100.0),
                                Arguments.arguments("USD", "", 100.0),
                                Arguments.arguments("USD", "EURO", 100.0),
                                Arguments.arguments("USD", "GBP", 100.0));
        }

        @Test
        void testConvertWithEmptyValue() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.get(REQUEST_URL).queryParam("source", "USD")
                                .queryParam("target", "EUR").queryParam("value", ""))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testConvertWithUnavailableExternalApi() throws Exception {
                when(currencyRateService.getCurrencyRate("USD", "EUR"))
                                .thenThrow(new RestClientException("N/A"));
                performRequest("USD", "EUR", 100).andExpect(status().isInternalServerError());
        }

        private ResultActions performRequest(String source, String target, double value)
                        throws Exception {
                return mockMvc.perform(generateRequest(source, target, value));
        }

        private MockHttpServletRequestBuilder generateRequest(String source, String target,
                        double value) {
                return MockMvcRequestBuilders.get(REQUEST_URL).queryParam("source", source)
                                .queryParam("target", target)
                                .queryParam("value", String.valueOf(value));
        }

}

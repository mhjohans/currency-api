package mhjohans.currency_api.controller;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import mhjohans.currency_api.service.ConversionService;

@WebMvcTest(ConversionController.class)
public class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversionService conversionService;

    @Test
    void testConvert() throws Exception {
        when(conversionService.convertCurrency(anyString(), anyString(), anyDouble())).thenReturn("$110.00");
        mockMvc.perform(MockMvcRequestBuilders.get("/currency-api/convert?from=USD&to=EUR&amount=100"))
                .andExpect(status().isOk())
                .andExpect(content().string("$110.00"));
    }

}

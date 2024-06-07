package mhjohans.currency_api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CurrencyRateApiClientConfiguration {

    @Bean
    public RestClient restClient(@Value("${currency_rate.base_url}") String baseUrl, 
                                @Value("${currency_rate.api_key}") String apiKey, 
                                RestClient.Builder restClientBuilder) {
        return restClientBuilder.baseUrl(baseUrl)
                                .defaultHeader("Authorization", "ApiKey " + apiKey)
                                .build();
    }

}

package mhjohans.currency_api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CurrencyRateApiClientConfiguration {

    @Bean
    public RestClient restClient(@Value("${currency_rates_api.base_url}") String baseUrl, @Value("${currency_rates_api.key}") String apiKey, RestClient.Builder restClientBuilder) {
        return restClientBuilder.baseUrl(baseUrl).defaultHeader("Authorization", "ApiKey " + apiKey).build();
    }

}

package mhjohans.currency_api.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    /**
     * Creates a REST client with the provided base URL, API key and API key authentication header used in the 
     * external currency rates API.
     */
    @Bean
    RestClient restClient(@Value("${currency-rates-api.base-url}") String baseUrl,
            @Value("${currency-rates-api.key}") String apiKey,
            RestClient.Builder restClientBuilder) {
        return restClientBuilder.baseUrl(baseUrl).defaultHeader("Authorization", "ApiKey " + apiKey)
                .build();
    }

}

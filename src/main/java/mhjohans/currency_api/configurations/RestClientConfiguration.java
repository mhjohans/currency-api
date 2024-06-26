package mhjohans.currency_api.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    RestClient restClient(@Value("${currency-rates-api.base-url}") String baseUrl,
            @Value("${currency-rates-api.key}") String apiKey,
            RestClient.Builder restClientBuilder) {
        return restClientBuilder.baseUrl(baseUrl).defaultHeader("Authorization", "ApiKey " + apiKey)
                .build();
    }

}

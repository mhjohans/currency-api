package mhjohans.currency_api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import jakarta.annotation.PostConstruct;
import mhjohans.currency_api.model.CurrencyRate;

@Service
class CurrencyRateService {
    
    @Value("${currency_rate.base_url}")
    private String baseUrl;
    @Value("${currency_rate.api_key}")
    private String apiKey;
    @Autowired
    private RestClient.Builder restClientBuilder;
    private RestClient restClient;
    
    double getCurrencyRate(final String from, final String to) {
        return restClient.get().uri("/{from}/{to}", from, to).retrieve().body(CurrencyRate.class).quote();
    }

    @PostConstruct
    private void init() {
        restClient = restClientBuilder.baseUrl(baseUrl).defaultHeader("Authorization", "ApiKey " + apiKey).build();
    }

}

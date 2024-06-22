package mhjohans.currency_api.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

        @Value("${security.api_key}")
        private String requiredApiKey;

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(authorizationCustomizer -> authorizationCustomizer
                                // Enable API key authentication
                                .anyRequest().authenticated()).httpBasic(Customizer.withDefaults())
                                .sessionManagement(
                                                sessionManagementConfigurer -> sessionManagementConfigurer
                                                                .sessionCreationPolicy(
                                                                                SessionCreationPolicy.STATELESS))
                                .addFilterBefore(new ApiKeyAuthenticationFilter(requiredApiKey),
                                                UsernamePasswordAuthenticationFilter.class)
                                // Enable CSRF protection
                                .csrf(csrfConfigurer -> csrfConfigurer.csrfTokenRepository(
                                                CookieCsrfTokenRepository.withHttpOnlyFalse()))
                                // Enable CSP that allows only the same origin for all resources
                                .headers(headersConfigurer -> headersConfigurer
                                                .contentSecurityPolicy(csp -> csp.policyDirectives(
                                                                "default-src 'self'")));
                return http.build();
        }

}

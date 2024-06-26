package mhjohans.currency_api.configurations.security;

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
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

        @Value("${security.api-key}")
        private String requiredApiKey;

        /**
         * Configures the security settings for the application's REST API.
         * - API key authentication is enabled for all requests.
         * - CSRF protection is enabled, but not active because is is only used when an method that
         * can alter the state is called, such as POST.
         * - CSP protection is enabled to allow only the same origin for all resources.
         */
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(authorizationCustomizer -> authorizationCustomizer
                                // Enable API key authentication
                                .anyRequest().authenticated()).httpBasic(Customizer.withDefaults())
                                .sessionManagement(
                                                sessionManagementConfigurer -> sessionManagementConfigurer
                                                                .sessionCreationPolicy(
                                                                                SessionCreationPolicy.STATELESS))
                                .addFilterBefore(
                                                new ApiKeyAuthenticationFilter(requiredApiKey,
                                                                AnyRequestMatcher.INSTANCE),
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

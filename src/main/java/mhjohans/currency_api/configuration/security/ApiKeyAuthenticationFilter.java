package mhjohans.currency_api.configuration.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final String requiredApiKey;

    public ApiKeyAuthenticationFilter(String requiredApiKey, RequestMatcher requestMatcher) {
        super(requestMatcher);
        this.requiredApiKey = requiredApiKey;
        setAuthenticationManager(this::authenticate);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null) {
            throw new BadCredentialsException("Missing API Key");
        }
        return getAuthenticationManager().authenticate(new ApiKeyAuthenticationToken(apiKey));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    private Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        if (authentication instanceof ApiKeyAuthenticationToken apiKeyAuthenticationToken
                && apiKeyAuthenticationToken.getApiKey().equals(requiredApiKey)) {
            return apiKeyAuthenticationToken;
        }
        throw new BadCredentialsException("Invalid API Key");
    }

}

package mhjohans.currency_api.configuration.security;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthenticationFilter extends GenericFilterBean {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final String requiredApiKey;

    public ApiKeyAuthenticationFilter(String requiredApiKey) {
        this.requiredApiKey = requiredApiKey;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            Authentication authentication = getAuthentication((HttpServletRequest) request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try (PrintWriter writer = httpResponse.getWriter()) {
                writer.print(e.getMessage());
                writer.flush();
            }
        }
        filterChain.doFilter(request, response);
    }

    private Authentication getAuthentication(HttpServletRequest request)
            throws AuthenticationException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null) {
            throw new BadCredentialsException("Missing API Key");
        }
        if (!apiKey.equals(requiredApiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }
        return new ApiKeyAuthenticationToken(apiKey);
    }

}

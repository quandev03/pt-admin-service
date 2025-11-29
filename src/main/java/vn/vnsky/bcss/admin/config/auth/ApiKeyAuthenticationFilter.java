package vn.vnsky.bcss.admin.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "x-api-key";

    private final RequestMatcher requestMatcher;
    private final String apiKey;

    public ApiKeyAuthenticationFilter(RequestMatcher requestMatcher, String apiKey) {
        this.requestMatcher = requestMatcher;
        this.apiKey = apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requestMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!StringUtils.hasText(apiKey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "API key is not configured");
            return;
        }
        String headerValue = request.getHeader(API_KEY_HEADER);
        if (!StringUtils.hasText(headerValue) || !apiKey.equals(headerValue)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API key");
            return;
        }
        filterChain.doFilter(request, response);
    }
}


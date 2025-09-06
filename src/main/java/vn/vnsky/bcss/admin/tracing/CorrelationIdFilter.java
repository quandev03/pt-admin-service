package vn.vnsky.bcss.admin.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import vn.vnsky.bcss.admin.constant.TracingContextEnum;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private final SecureRandom random = new SecureRandom();

    private String randomUUID() {
        return new UUID(random.nextLong(), random.nextLong()).toString().replace("-", "");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader(TracingContextEnum.X_CORRELATION_ID.getHeaderKey()) != null)
            ThreadContext.put(TracingContextEnum.X_CORRELATION_ID.getThreadKey(), request.getHeader(TracingContextEnum.X_CORRELATION_ID.getHeaderKey()));
        ThreadContext.putIfNull(TracingContextEnum.X_CORRELATION_ID.getThreadKey(), this.randomUUID());
        String contentType = request.getContentType();
        if (StringUtils.hasText(contentType)) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            if (logger.isDebugEnabled() && (
                    mediaType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                    mediaType.isCompatibleWith(MediaType.APPLICATION_XML) ||
                    mediaType.isCompatibleWith(MediaType.TEXT_HTML))
            ) {
                ContentCachingRequestWrapper contentCachingRequestWrapper = new ContentCachingRequestWrapper(request);
                ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
                filterChain.doFilter(contentCachingRequestWrapper, contentCachingResponseWrapper);
                this.logRequest(contentCachingRequestWrapper);
                this.logResponse(contentCachingResponseWrapper);
                contentCachingResponseWrapper.copyBodyToResponse();
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
        ThreadContext.clearAll();
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        log.debug("[LOGGING_REQUEST] - url : {}", request.getRequestURI());
        log.debug("[LOGGING_REQUEST] - method : {}", request.getMethod());
        if (log.isTraceEnabled()) {
            request.getHeaderNames().asIterator()
                    .forEachRemaining(headerName ->
                            log.trace("[LOGGING_REQUEST] - header : {} = {}", headerName, request.getHeader(headerName)));
        }
        log.debug("[LOGGING_REQUEST] - body : \n{}", new String(request.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        log.debug("[LOGGING_RESPONSE] - status : {}", response.getStatus());
        if (log.isTraceEnabled()) {
            response.getHeaderNames().iterator()
                    .forEachRemaining(headerName ->
                            log.trace("[LOGGING_RESPONSE] - header : {} = {}", headerName, response.getHeader(headerName)));
        }
        String contentType = response.getContentType();
        if (StringUtils.hasText(contentType)) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            if (logger.isDebugEnabled() && (
                    mediaType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                    mediaType.isCompatibleWith(MediaType.APPLICATION_XML) ||
                    mediaType.isCompatibleWith(MediaType.TEXT_HTML))
            ) {
                log.debug("[LOGGING_RESPONSE] - body : \n{}", new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
            }
        }
    }
}

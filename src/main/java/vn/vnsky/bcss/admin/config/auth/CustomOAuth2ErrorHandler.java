package vn.vnsky.bcss.admin.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomOAuth2ErrorHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    private final MessageSource messageSource;

    public CustomOAuth2ErrorHandler(ObjectMapper objectMapper, MessageSource messageSource) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status.value());
        PrintWriter out = response.getWriter();
        String message;
        ProblemDetail problemDetail;
        if (exception instanceof OAuth2AuthenticationException oAuth2AuthenticationException) {
            if (StringUtils.hasText(oAuth2AuthenticationException.getError().getUri())) {
                message = this.messageSource.getMessage(oAuth2AuthenticationException.getError().getDescription(), null, request.getLocale());
                problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
                Map<String, String> details = new HashMap<>();
                details.put("field", oAuth2AuthenticationException.getError().getUri());
                details.put("detail", message);
                problemDetail.setProperty("errors", Collections.singletonList(details));
            } else {
                message = StringUtils.hasText(oAuth2AuthenticationException.getError().getDescription()) ? oAuth2AuthenticationException.getError().getDescription() :
                        oAuth2AuthenticationException.getError().getErrorCode();
                problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
            }
        } else {
            message = this.messageSource.getMessage(exception.getMessage(), null, request.getLocale());
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, message);
        }
        out.print(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(problemDetail));
        out.flush();
    }
}

package vn.vnsky.bcss.admin.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import vn.vnsky.bcss.admin.constant.TracingContextEnum;
import vn.vnsky.bcss.admin.error.ExceptionTranslator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    private final MessageSource messageSource;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper, MessageSource messageSource) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.error("Authorization error", accessDeniedException);
        HttpStatus status = HttpStatus.FORBIDDEN;
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status.value());
        PrintWriter out = response.getWriter();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, accessDeniedException.getMessage());
        problemDetail.setTitle(messageSource.getMessage("error.title.authorization", null, LocaleContextHolder.getLocale()));
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(ExceptionTranslator.CORRELATION_ID_KEY, corrId);
        out.print(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(problemDetail));
        out.flush();
    }
}

package vn.vnsky.bcss.admin.error;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.vnsky.bcss.admin.constant.TracingContextEnum;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class ExceptionTranslator extends ResponseEntityExceptionHandler {

    public static final String INPUT_INVALID_TITLE_KEY = "error.message.data-invalid";

    private static final String INTERNAL_ERROR_TITLE_KEY = "error.title.internal";

    private static final String VIOLATIONS_KEY = "errors";

    public static final String CORRELATION_ID_KEY = "correlationId";

    @Autowired
    public ExceptionTranslator(MessageSource messageSource) {
        this.setMessageSource(messageSource);
    }

    @ExceptionHandler({BusinessException.class})
    public ProblemDetail handleBusinessException(BusinessException ex, @NonNull WebRequest request) {
        log.error("Business error: ", ex);
        Locale locale = request.getLocale();
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(ex.getMessage(), ex.getParams(), locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return problemDetail;
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex, @NonNull WebRequest request) {
        log.error("Entity not found error: ", ex);
        Locale locale = request.getLocale();
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(ex.getMessage(), null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return problemDetail;
    }

    @ExceptionHandler({EntityExistsException.class})
    public ProblemDetail handleEntityExistedException(EntityExistsException ex, @NonNull WebRequest request) {
        log.error("Entity existed error: ", ex);
        Locale locale = request.getLocale();
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(ex.getMessage(), null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        log.error("Method arguments error: ", ex);
        Locale locale = request.getLocale();
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(INPUT_INVALID_TITLE_KEY, null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        if (ex.getBindingResult().hasErrors()) {
            List<FieldsValidationEntry> errorDetail = ex.getFieldErrors()
                    .stream()
                    .map(e -> FieldsValidationEntry.builder()
                            .field(e.getField())
                            .detail(this.getMessageSource().getMessage(Objects.requireNonNull(e.getDefaultMessage()), null, locale))
                            .build())
                    .toList();
            problemDetail.setProperty(VIOLATIONS_KEY, errorDetail);
        }
        return handleExceptionInternal(ex, problemDetail, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex, @NonNull WebRequest request) {
        log.error("API input error: ", ex);
        Locale locale = request.getLocale();
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(INPUT_INVALID_TITLE_KEY, null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        if (!ObjectUtils.isEmpty(ex.getConstraintViolations())) {
            List<FieldsValidationEntry> translatedViolations = ex.getConstraintViolations()
                    .stream()
                    .map(e -> FieldsValidationEntry.builder()
                            .field(e.getPropertyPath().toString())
                            .detail(this.getMessageSource().getMessage(e.getMessage(), null, locale))
                            .build())
                    .toList();
            problemDetail.setProperty(VIOLATIONS_KEY, translatedViolations);
        }
        return problemDetail;
    }

    @ExceptionHandler({FieldsValidationException.class})
    public ProblemDetail handleFieldsValidationException(FieldsValidationException ex, @NonNull WebRequest request) {
        log.error("API fields input error: ", ex);
        Locale locale = request.getLocale();
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(INPUT_INVALID_TITLE_KEY, null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        if (!ObjectUtils.isEmpty(ex.getFieldErrors())) {
            List<FieldsValidationEntry> translatedViolations = ex.getFieldErrors()
                    .entrySet()
                    .stream()
                    .map(e -> FieldsValidationEntry.builder()
                            .field(e.getKey())
                            .detail(getMessageSource().getMessage(e.getValue(), null, locale))
                            .build())
                    .toList();
            problemDetail.setProperty(VIOLATIONS_KEY, translatedViolations);
        }
        return problemDetail;
    }

    @ExceptionHandler({AuthenticationException.class})
    public ProblemDetail handleAuthentication(AuthenticationException ex, @NonNull WebRequest request) {
        log.error("Authentication error: ", ex);
        Locale locale = request.getLocale();
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        String title = Objects.requireNonNull(this.getMessageSource())
                .getMessage("error.title.authentication", null, locale);
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(ex.getMessage(), null, locale);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return problemDetail;
    }

    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<Object> handleAuthorization(AccessDeniedException ex, @NonNull WebRequest request) {
        log.error("Authorization error: ", ex);
        Locale locale = request.getLocale();
        HttpStatus status = HttpStatus.FORBIDDEN;
        String title = Objects.requireNonNull(this.getMessageSource())
                .getMessage("error.title.authorization", null, locale);
        String detail = Objects.requireNonNull(this.getMessageSource())
                .getMessage(ex.getMessage(), null, locale);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return super.handleExceptionInternal(ex, problemDetail, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ProblemDetail handleNoSuchElementException(NoSuchElementException ex, @NonNull WebRequest request) {
        log.error("Not found error: ", ex);
        Locale locale = request.getLocale();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        String title = Objects.requireNonNull(this.getMessageSource())
                .getMessage(INTERNAL_ERROR_TITLE_KEY, null, locale);
        problemDetail.setTitle(title);
        problemDetail.setDetail(title);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return problemDetail;
    }

    @ExceptionHandler({ConcurrencyFailureException.class})
    public ProblemDetail handleConcurrencyFailure(ConcurrencyFailureException ex, @NonNull WebRequest request) {
        log.error("Concurrency error: ", ex);
        Locale locale = request.getLocale();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        String title = Objects.requireNonNull(this.getMessageSource())
                .getMessage(INTERNAL_ERROR_TITLE_KEY, null, locale);
        problemDetail.setTitle(title);
        problemDetail.setDetail(title);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return problemDetail;
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, @NonNull WebRequest request) {
        log.error("Internal error: ", ex);
        Locale locale = request.getLocale();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        String title = Objects.requireNonNull(this.getMessageSource())
                .getMessage(INTERNAL_ERROR_TITLE_KEY, null, locale);
        problemDetail.setTitle(title);
        problemDetail.setDetail(title);
        final String corrId = ThreadContext.get(TracingContextEnum.X_CORRELATION_ID.getThreadKey());
        problemDetail.setProperty(CORRELATION_ID_KEY, corrId);
        return super.handleExceptionInternal(ex, problemDetail, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}

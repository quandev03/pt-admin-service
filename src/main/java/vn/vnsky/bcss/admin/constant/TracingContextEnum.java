package vn.vnsky.bcss.admin.constant;

import lombok.Getter;

@Getter
public enum TracingContextEnum {
    X_FORWARD_FOR("x-forwarded-for", "forwardIP"),
    X_REAL_IP("x-real-ip", "clientIP"),
    X_REQUEST_ID("x-request-id", "requestID"),
    X_CORRELATION_ID("X-Correlation-ID", "correlationID");

    private final String headerKey;
    private final String threadKey;

    TracingContextEnum(final String headerKey, final String threadKey) {
        this.headerKey = headerKey;
        this.threadKey = threadKey;
    }
}

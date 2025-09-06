package vn.vnsky.bcss.admin.error;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "MK/ERROR/";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "SYSTEM");

    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "CONSTRAINT-VIOLATION");
    public static final URI ENTITY_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "RESOURCE-NOT-FOUND");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "INVALID-PASSWORD");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "EMAIL-ALREADY-USED");

    public static final URI OTP_MISMATCHED_TYPE = URI.create(PROBLEM_BASE_URL + "OTP-MISMATCHED");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "LOGIN-ALREADY-USED");
    public static final URI EMAIL_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "EMAIL-NOT-FOUND");

    public static final URI CLIENT_CODE_REQUIRE_TYPE = URI.create(PROBLEM_BASE_URL + "CLIENT-CODE-REQUIRE");

    public static final URI SESSION_EXPIRED_TYPE = URI.create(PROBLEM_BASE_URL + "SESSION-EXPIRED");
    public static final URI FORCE_UPDATE_APP_TYPE = URI.create(PROBLEM_BASE_URL + "FORCE_UPDATE_APP");

    private ErrorConstants() {
    }
}

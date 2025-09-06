package vn.vnsky.bcss.admin.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;


/**
 * Utility class for generating random Strings.
 */
@Slf4j
public final class RandomUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();


    private RandomUtil() {
    }

    public static String generateNewToken() {
        byte[] randomBytes = new byte[100];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Generate a password.
     *
     * @return the generated password.
     */
    public static String generateCode() {
        byte[] bytes = new byte[6];
        secureRandom.nextBytes(bytes);
        return base64Encoder.encodeToString(bytes);
    }
}

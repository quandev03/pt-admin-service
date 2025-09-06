package vn.vnsky.bcss.admin.util;


import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class StringUtil {

    private final SecureRandom RD = new SecureRandom();

    private static final DecimalFormat decimalFormat = new DecimalFormat("000000");
    private final char[] SPECIAL_CHARS = ("@$!%:;*?&").toCharArray();
    private final char[] UPPER_CHARS = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
    private final char[] LOWER_CHARS = ("abcdefghijklmnopqrstuvwxyz").toCharArray();
    private final char[] NUMBER_CHARS = ("01234567890").toCharArray();

    public String camelToSnake(String str) {

        // Empty String
        StringBuilder result = new StringBuilder();

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result.append(Character.toLowerCase(c));

        // Traverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            // Check if the character is upper case
            // then append '_' and such character
            // (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            }

            // If the character is lower case then
            // add such character into result string
            else {
                result.append(ch);
            }
        }

        // return the result
        return result.toString();
    }

    public String generateRandomStr(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        return RD.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 2; ++i) {
            sb.append(SPECIAL_CHARS[RD.nextInt(SPECIAL_CHARS.length)]);
        }
        for (int i = 0; i < 6; ++i) {
            sb.append(UPPER_CHARS[RD.nextInt(UPPER_CHARS.length)]);
        }
        for (int i = 0; i < 6; ++i) {
            sb.append(LOWER_CHARS[RD.nextInt(LOWER_CHARS.length)]);
        }
        for (int i = 0; i < 2; ++i) {
            sb.append(NUMBER_CHARS[RD.nextInt(NUMBER_CHARS.length)]);
        }
        List<Character> chars = sb.chars()
                .mapToObj(e -> (char) e)
                .collect(Collectors.toList());
        Collections.shuffle(chars);
        return chars.stream()
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public String generateOTP() {
        return decimalFormat.format(RD.nextInt(999999));
    }

    public String generateOrderCode() {
        return decimalFormat.format(RD.nextInt(999999999));
    }

    public static String buildLikeOperator(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return "%".concat(value.trim().toUpperCase()).concat("%");
    }

    public static String buildLikeOperatorLower(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return "%".concat(value.trim().toLowerCase()).concat("%");
    }

}

package vn.vnsky.bcss.admin.constant;

import lombok.experimental.UtilityClass;

/**
 * Application constants.
 */
@UtilityClass
public class AuthConstants {

    public final String SPRING_PROFILE_DEVELOPMENT = "dev";

    public static final String SYSTEM_ACCOUNT = "system";

    public static final String VI_LANGUAGE = "vi-VN";

    public static final String US_LANGUAGE = "en-US";

    public static final String OWNER_TYPE = "owner";

    public static final String WEB_CODE = "CLIENT";

    public static final String VNSKY_CLIENT_ID = "000000000000";

    public static final String VNSKY_CLIENT_ALIAS = "VNSKY";

    public static final String THIRD_PARTY_CLIENT_ID = "3RD000000000";

    @UtilityClass
    public static class DateTimeFmt {

        public static final String YYYY_MM_DD_HH_MM_SS_ISO = "yyyy-MM-dd HH:mm:ss";
        public static final String DD_MM_YYYY_SLASH = "dd/MM/yyyy";

        public static final String DD_MM_SLASH = "dd/MM";
        public static final String PATTERN_DATE_FILE_NAME = "yyyyMMdd";

        public static final String DD_MM_YYYY_HH_MM_SS_ISO = "dd/MM/yyyy HH:mm:ss";
        public static final String DD_MM_YYYY_HH_MM_ISO = "dd/MM/yyyy HH:mm";
        public static final String HH_MM_ISO = "HH:mm";

    }

    @UtilityClass
    public static class ValidationUserField {
        public static final String PHONE_NUMBER_OPTIONAL_REGEX = "[0-9]{10,11}|^\\s*$";
        public static final String PHONE_NUMBER_REGEXP = "^[0-9]{10,11}";
        public static final String PASSPORT_REGEXP = "^[A-Za-z0-9]{1,10}$";
        public static final String EMAIL_REGEXP = "[a-zA-Z0-9][\\w-\\.]+@[a-zA-Z0-9][\\w-\\.]+(\\.[a-zA-Z0-9]+)$";
        public static final String CCCD_REGEXP = "^[0-9]{12}$";
        public static final String TAX_CODE_REGEXP = "^[0-9]{0,50}";
        public static final String NON_VIETNAMESE_CHARACTER = "^[^ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼẾỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\\n]+";
        public static final String NON_SPACE = "\\S*";
        public static final String NON_SPECIAL_CHARACTER = "^[a-zA-Z0-9\\+]*$";
    }

    @UtilityClass
    public static class EmailContent {
        public static final String MAIL_SUBJECT_ACTIVATE = "[VNSKY_SERVICE] CẤP TÀI KHOẢN ĐĂNG NHẬP";
        public static final String MAIL_SUBJECT_FORGOT = "[VNSKY_SERVICE] ĐẶT LẠI MẬT KHẨU TÀI KHOẢN";
        public static final String MAIL_EXPIRE_TIME = "5 phút";
    }

    @UtilityClass
    public class CommonSymbol {

        public final String SPACE = " ";

        public final String DOT = ".";

        public final String BACKSLASH = "\\";

        public final String COMMA = ",";

        public final String DASH = "-";

        public final String SHIFT_DASH = "_";

        public final String COLON = ":";

        public final String FORWARD_SLASH = "/";

        public final String ASTERISK = "*";
    }

    public static class ModelStatus {

        public static final int ACTIVE = 1;
        public static final int INACTIVE = 0;

        private ModelStatus() {
        }
    }

    public static class QueryParams {

        public static final String USER_ID_PARAM = "userId";
        public static final String LIMIT_PARAM = "limit";
        public static final String OFFSET_PARAM = "offset";
        public static final String TERM_PARAM = "term";

        private QueryParams() {
        }

    }

}

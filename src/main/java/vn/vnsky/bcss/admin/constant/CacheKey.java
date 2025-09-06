package vn.vnsky.bcss.admin.constant;

import lombok.experimental.UtilityClass;

/**
 * @author vis-thanhvt
 * @created 06/04/2023 - 1:59 CH
 * @project str-auth
 * @since 1.0
 **/
@UtilityClass
public class CacheKey {

    public static final String CACHE_KEY_BASE = "VNSKY";
    public static final String CACHE_KEY_APP = "ADMIN";
    public static final String CACHE_KEY_SEPARATOR = "::";
    public static final String CACHE_KEY_PREFIX = CACHE_KEY_BASE + CACHE_KEY_SEPARATOR + CACHE_KEY_APP + CACHE_KEY_SEPARATOR;

    public static final String PARAM_PREFIX = "PARAM";
    public static final String USER_FORGOT_PASSWORD_TOKEN_PREFIX = "USER_FORGOT_PASSWORD_TOKEN";
    public static final String USER_INFO_PREFIX = "USER_INFO";
    public static final String USER_POLICY_PREFIX = "USER_POLICY";
    public static final String API_ACL_PREFIX = "API_ACL";

}

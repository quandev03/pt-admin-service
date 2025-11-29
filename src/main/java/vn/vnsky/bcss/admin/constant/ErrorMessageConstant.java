package vn.vnsky.bcss.admin.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessageConstant {

    public static final String OBJECT_NOT_FOUND = "error.message.not-found";

    public static final String OBJECT_ID_NOT_FOUND = "error.message.object.id-not-exist";

    public static final String OBJECT_APP_ID_AND_CODE_EXISTED = "error.message.object.app-id-and-code-existed";

    public static final String OBJECT_ACTION_ID_DUPLICATE = "error.message.object.action-id-duplicated";

    public static final String ACTION_ID_NOT_FOUND = "error.message.action.id-not-found";

    public static final String GROUP_NOT_FOUND = "error.message.group.id-not-existed";

    public static final String ROLE_NOT_FOUND = "error.message.role.id-not-existed";

    public static final String USER_NOT_FOUND = "error.message.user.id-not-existed";

    public static final String USER_NAME_EXIST = "error.message.user.username-existed";

    public static final String EMAIL_EXIST = "error.message.user.email-existed";

    public static final String EMAIL_NOT_EXIST = "error.message.user.email-is-not-existed";

    public static final String REQUIRED_ROLE_GROUP = "error.message.user.exist-group-role";

    public static final String INVALID_LOGIN_METHOD = "error.message.user.invalid-login-method";

    public static final String TOKEN_EXPIRED = "error.message.token-expired";

    public static final String CLIENT_NOT_FOUND = "AbstractUserDetailsAuthenticationProvider.clientIdentityNotFound";

    public static final String USERNAME_NOT_FOUND = "AbstractUserDetailsAuthenticationProvider.userNotFound";

    public static final String SYSTEM_ERROR = "error.message.internal-server-error";

    public static final String DEPARTMENT_NOT_FOUND = "error.message.department.id-not-existed";

    public static final String CAN_NOT_DELETE_AUTHORIZED_ACTION = "error.message.can-not-delete-authorized-action";

    public static final String BAD_REQUEST = "error.message.bad-request";

    public static final String GROUP_USER_ALREADY_EXISTS = "error.message.group-user.existed";
}

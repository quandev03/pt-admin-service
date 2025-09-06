package vn.vnsky.bcss.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserLoginMethod {
    LOGIN_USERNAME(1, "Username"),
    LOGIN_GOOGLE(2, "Google")
    ;

    private final int value;

    private final String name;
}

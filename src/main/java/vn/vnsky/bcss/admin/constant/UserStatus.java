package vn.vnsky.bcss.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    INACTIVE(0),
    ACTIVE(1),
    LOCKED(2);

    private final Integer value;
}

package vn.vnsky.bcss.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuickSearchKeyOption {
    EQUAL(" %COLUMN_NAME% = :term"),
    LIKE(" %COLUMN_NAME% like concat(concat('%', :term), '%')"),
    DATE(" %COLUMN_NAME% like concat(concat('%', :term), '%') ");

    private final String value;
}

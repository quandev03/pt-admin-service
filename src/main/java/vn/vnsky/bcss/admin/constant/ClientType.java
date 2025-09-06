package vn.vnsky.bcss.admin.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public enum ClientType {

    VNSKY("VNSKY", Pattern.compile("^000000000000$")),
    PARTNER("PARTNER", Pattern.compile("^(?!000000000000$)\\d{12}$")),
    PARTNER_AND_VNSKY("PARTNER_AND_VNSKY", Pattern.compile("^\\d{12}$")),
    THIRD_PARTY("THIRD_PARTY", Pattern.compile("^3RD\\d{9}$"));

    private final String value;

    private final Pattern idPattern;

}

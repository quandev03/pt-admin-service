package vn.vnsky.bcss.admin.error;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Map;

@Getter
@Setter
public class FieldsValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> fieldErrors;

    public FieldsValidationException(Map<String, String> fieldErrors) {
        super("Validation error");
        this.fieldErrors = fieldErrors;
    }
}

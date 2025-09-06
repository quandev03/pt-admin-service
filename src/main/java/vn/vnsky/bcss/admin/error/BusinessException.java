package vn.vnsky.bcss.admin.error;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String[] params;

    public BusinessException(String message, String... params) {
        super(message);
        this.params = params;
    }

}

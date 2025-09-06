package vn.vnsky.bcss.admin.data;

import org.junit.platform.commons.JUnitException;

public class JsonParsingException extends JUnitException {

    public JsonParsingException(String message) {
        super(message);
    }

    public JsonParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}

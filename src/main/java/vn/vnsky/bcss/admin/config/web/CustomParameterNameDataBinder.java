package vn.vnsky.bcss.admin.config.web;

import jakarta.servlet.ServletRequest;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import java.util.Map;
import java.util.Objects;

/**
 * ServletRequestDataBinder which supports fields renaming using {@link org.springframework.web.bind.annotation.BindParam}
 *
 * @author jkee
 */
public class CustomParameterNameDataBinder extends ExtendedServletRequestDataBinder {

    private final Map<String, String> renameMapping;

    public CustomParameterNameDataBinder(Object target, String objectName, Map<String, String> renameMapping) {
        super(target, objectName);
        this.renameMapping = renameMapping;
    }

    @Override
    protected void addBindValues(@NonNull MutablePropertyValues mpvs, @NonNull ServletRequest request) {
        super.addBindValues(mpvs, request);
        for (Map.Entry<String, String> entry : renameMapping.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();
            if (mpvs.contains(from)) {
                mpvs.add(to, Objects.requireNonNull(mpvs.getPropertyValue(from)).getValue());
            }
        }
    }

}
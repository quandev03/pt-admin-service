package vn.vnsky.bcss.admin.util;

import jakarta.persistence.AttributeConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbColumnMapper {

    String value() default "";

    Class<? extends AttributeConverter> converter() default None.class;

    interface None extends AttributeConverter<String, Object> {

    }

}
